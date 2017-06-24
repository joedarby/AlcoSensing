package com.joedarby.alcosensing1.Helpers;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.joedarby.alcosensing1.BackboneApplication;
import com.joedarby.alcosensing1.BuildConfig;
import com.joedarby.alcosensing1.Data.AppPrefs;
import com.joedarby.alcosensing1.R;
import com.joedarby.alcosensing1.Services.ContinuousSensingService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusHelper {

    public static final int BATTERY_OK = 0;
    public static final int BATTERY_LOW = 1;
    public static final int BATTERY_CRITICAL = 2;


    public static boolean checkWifiOnAndConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        boolean isWiFi = false;

        try {
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (NullPointerException e) {
            //
        }


        return isConnected && isWiFi;
    }

    public static boolean isAnyNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static int getBatteryLevel(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;

        if (batteryPct > 0.30) {
            return BATTERY_OK;
        } else if (batteryPct > 0.15) {
            return BATTERY_LOW;
        } else {
            return BATTERY_CRITICAL;
        }
    }

    public static String getCurrentDateTime () {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
        return format.format(new Date());
    }

    public static String getFileLabel (Context context) {
        AppPrefs prefs = AppPrefs.getInstance(context);
        return prefs.getUserID() + "-" + getCurrentDateTime() + "-";
    }

    public static void updateAppVersionStatus(Context context) {
        final String tag = "UpdateAppVersionStatus";
        final AppPrefs prefs = AppPrefs.getInstance(context);
        String AWS_COGNITO_POOL_ID = "[OBSCURED]";
        String AWS_BUCKET_ID = "alcosensing-version-check";
        String AWS_FILE_KEY = "alcosensing-version.json";
        final File file = new File(context.getFilesDir().getAbsolutePath()+ "/app-version.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                AWS_COGNITO_POOL_ID, // Identity Pool ID
                Regions.EU_WEST_2 // Region
        );
        if (isAnyNetworkConnected(context)) {
            Log.e(tag, "Attempting check");
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            TransferUtility transferUtility = new TransferUtility(s3, context);
            TransferObserver observer = transferUtility.download(AWS_BUCKET_ID, AWS_FILE_KEY, file );
            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (state.equals(TransferState.COMPLETED)) {
                        Log.e(tag, "File retrieved");
                        Gson gson = new Gson();
                        try {
                            FileReader reader = new FileReader(file);
                            try {
                                AppVersion version = gson.fromJson(reader, AppVersion.class);
                                if (version.versionNumber > BuildConfig.VERSION_CODE
                                        && version.critical) {
                                    prefs.setIsAppVersionOK(false);
                                    Log.e(tag, "Version not OK, updated needed");
                                    sendUpdateNeededNotification(BackboneApplication.getContext());
                                } else {
                                    prefs.setIsAppVersionOK(true);
                                    Log.e(tag, "Version is ok. No update required");
                                }
                            } catch (JsonSyntaxException | JsonIOException e) {
                                Crashlytics.logException(e);
                            }

                        } catch (FileNotFoundException e) {
                            Crashlytics.logException(e);
                        }

                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                }

                @Override
                public void onError(int id, Exception ex) {
                    Crashlytics.logException(ex);

                }
            });

        }
    }

    public static class AppVersion {
        public int versionNumber;
        public boolean critical;
    }

    private static void sendUpdateNeededNotification(Context context) {
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        long[] pattern = {600,600,600};

        Notification updateNotification = new NotificationCompat.Builder(context)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Critical update required from the Play Store. No more surveys will be sent until you update."))
                .setContentTitle("AlcoSensing")
                .setSmallIcon(R.mipmap.pint_glass)
                .setWhen(System.currentTimeMillis() + 5000)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setVibrate(pattern)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                .setAutoCancel(true)
                .setOngoing(false)
                .build();


        nManager.notify(3, updateNotification);

    }

    public static boolean isSensingServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ContinuousSensingService.class.getName().equals(service.service.getClassName())) {
                Log.e("Status Helper", "Sensing Service Running");
                return true;
            }
        }
        Log.e("Status Helper", "Sensing service not running");
        return false;
    }
}
