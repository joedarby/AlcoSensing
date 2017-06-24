package com.joedarby.alcosensing1.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
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
import com.joedarby.alcosensing1.Data.AppPrefs;
import com.joedarby.alcosensing1.Helpers.AlarmHelper;
import com.joedarby.alcosensing1.Helpers.StatusHelper;

import java.io.File;


public class ConsentUpload extends Service {

    private static final String AWS_COGNITO_POOL_ID = "[OBSCURED]";
    private static final String AWS_BUCKET_ID = "jdarby-msc";

    AppPrefs prefs;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = AppPrefs.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initiateUpload();

        return START_STICKY;
    }


    public void initiateUpload() {
        if(prefs.isConsentUploadPending()) {
            if (StatusHelper.isAnyNetworkConnected(this)) {
                Log.e("DataUploadService", "Attempting consent upload...");
                final File file = new File(getFilesDir().getAbsolutePath() + "/SensorData/data/" + prefs.getUserID() + "-" + "consent.json");
                final String key = file.getName();
                TransferObserver observer = uploadToAWS(key, file);

                observer.setTransferListener(new TransferListener(){

                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state.equals(TransferState.COMPLETED)) {
                            Log.e("DataUploadService", key + " uploaded");
                            file.delete();
                            prefs.setIsConsentUploadPending(false);
                            stopSelf();
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        //Do nothing
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Crashlytics.logException(ex);
                        setSelfAlarm(ConsentUpload.class);
                        stopSelf();
                    }
                });
            } else {
                setSelfAlarm(ConsentUpload.class);
                stopSelf();
            }

        } else {
            stopSelf();
        }
    }

    public TransferObserver uploadToAWS(final String key, final File uploadFile) {

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                this,
                AWS_COGNITO_POOL_ID, // Identity Pool ID
                Regions.EU_WEST_2 // Region
        );

        // Create an S3 client
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

        TransferUtility transferUtility = new TransferUtility(s3, this);

        final TransferObserver observer = transferUtility.upload(
                AWS_BUCKET_ID,     /* The bucket to upload to */
                key,    /* The key for the uploaded object */
                uploadFile        /* The file where the data to upload exists */
        );

        return observer;

    }


    protected void setSelfAlarm(Class classType) {
        AlarmHelper.get().setUploadRetryAlarm(classType);
    }
}
