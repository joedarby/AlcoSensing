package com.joedarby.alcosensing1.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.crashlytics.android.Crashlytics;
import com.joedarby.alcosensing1.Data.AppPrefs;
import com.joedarby.alcosensing1.Helpers.AlarmHelper;
import com.joedarby.alcosensing1.Helpers.StatusHelper;
import com.joedarby.alcosensing1.Helpers.TimeConstants;
import com.joedarby.alcosensing1.R;
import com.joedarby.alcosensing1.Sensing.SensingSession;

import org.sensingkit.sensingkitlib.SKException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.GZIPOutputStream;


public class ContinuousSensingService extends Service {

    private final IBinder mBinder = new LocalBinder();

    private SensingSession session;
    private NotificationManager nManager;
    private boolean sessionRunning;
    private AppPrefs prefs;
    private final BatteryBroadcastReceiver batteryBR = new BatteryBroadcastReceiver();


    public class LocalBinder extends Binder {
        ContinuousSensingService getService() {
            return ContinuousSensingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = AppPrefs.getInstance(this);
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            session = new SensingSession(this);
            session.start();
            sessionRunning = true;
            prefs.setIsSensing(true);
            setBatteryReceiver();
            prefs.setSensingStartTime(StatusHelper.getCurrentDateTime());
            startForeground(1, showSensingNotification());
        } catch (SKException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sessionRunning) {
                    try {
                        session.stop();
                        session.close();
                    } catch (SKException | IllegalStateException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                    prefs.setSensingTriggeredByUser(false);
                    try {
                        unregisterReceiver(batteryBR);
                    } catch (IllegalArgumentException e) {
                        //
                    }
                    nManager.cancel(1);
                    prefs.setIsSensing(false);
                    compressSensorData(ContinuousSensingService.this);
                    prefs.setSensingEndTime(new Date().getTime());
                    scheduleSurvey(ContinuousSensingService.this);
                }
                stopSelf();
            }
        }, TimeConstants.SENSING_PERIOD);
        return START_STICKY;
    }

    public void abortSensing(final boolean restart, final boolean batteryLow) {
        nManager.cancel(1);
        sessionRunning = false;

        //delay to make sure sensing isn't starting up (2 seconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    session.stop();
                    session.close();
                } catch (SKException | IllegalStateException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                prefs.setIsSensing(false);
                prefs.setSensingTriggeredByUser(false);
                Log.e("SensingAbortService", "Sensing Aborted");
                try {
                    unregisterReceiver(batteryBR);
                } catch (IllegalArgumentException e) {
                    //
                }

                if (batteryLow) {
                    compressSensorData(ContinuousSensingService.this);
                    prefs.setSensingEndTime(new Date().getTime());
                    scheduleSurvey(ContinuousSensingService.this);
                } else {
                    File folder = new File(getFilesDir().getAbsolutePath() + "/SensorData/data/");
                    for (final File child : folder.listFiles()) {
                        child.delete();
                    }
                }

                if (restart) {
                    Intent triggerIntent = new Intent(ContinuousSensingService.this, SensingTriggerService.class);
                    triggerIntent.putExtra("UserTriggered", true);
                    startService(triggerIntent);
                }
                stopSelf();
            }
        }, 1000*2);
    }

    private Notification showSensingNotification() {

        Intent abortIntent = new Intent(this, SensingAbortService.class);
        abortIntent.putExtra("restart", false);
        PendingIntent pendingIntent = PendingIntent.getService(ContinuousSensingService.this, 0, abortIntent, 0);

        Notification sensingNotification = new NotificationCompat.Builder(ContinuousSensingService.this)
                .setContentTitle("AlcoSensing")
                .setContentText("Currently gathering data")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSmallIcon(R.mipmap.pint_glass)
                .addAction(R.mipmap.pint_glass, "Stop for at least 3 hours", pendingIntent)
                .build();

        sensingNotification.flags = Notification.FLAG_ONGOING_EVENT;

       return sensingNotification; // 1
    }


    private void setBatteryReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        this.registerReceiver(batteryBR, filter);
    }

    public static void scheduleSurvey(Context context) {
        AppPrefs prefs = AppPrefs.getInstance(context);

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(prefs.getSensingEndTime());
        //commented out for testing

        //add 2 minutes for test, add 8 hours real life
        cal.add(Calendar.HOUR_OF_DAY, 8);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour > 22 || hour < 9) {
            if (hour > 22) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 0);
        }

        long scheduleTime = cal.getTimeInMillis();

        AlarmHelper.get().setSurveyAlarm(scheduleTime);
        prefs.setIsSurveyPending(true);
        Log.e("ContinuousSensService", "Survey set for: " + Long.toString(scheduleTime));

    }

    public static void compressSensorData(Context context) {
        File folder = new File(context.getFilesDir().getAbsolutePath() + "/SensorData/data/");
        for (final File child : folder.listFiles()) {

            try {
                String fileName = child.getAbsolutePath();
                String gzipFileName = fileName + ".gz";
                FileInputStream fis = new FileInputStream(fileName);
                FileOutputStream fos = new FileOutputStream(gzipFileName);
                GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
                byte[] buffer = new byte[512];
                int len;
                while((len=fis.read(buffer)) != -1){
                    gzipOS.write(buffer, 0, len);
                }
                //close resources
                gzipOS.close();
                fos.close();
                fis.close();
                child.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
