package com.joedarby.alcosensing1.Services;


import android.content.Intent;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.crashlytics.android.Crashlytics;
import com.joedarby.alcosensing1.BackboneApplication;
import com.joedarby.alcosensing1.Helpers.AlarmHelper;
import com.joedarby.alcosensing1.Helpers.StatusHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataUpload extends ConsentUpload {

    public static Intent getIntent() {
        Intent intent = new Intent(BackboneApplication.getContext(), DataUpload.class);
        return intent;
    }

    private List<Integer> transfers = new ArrayList<>();

    @Override
    public void initiateUpload() {

        if (prefs.isDataUploadPending()) {
            Log.e("DataUploadService", "Data to upload");
            if (StatusHelper.checkWifiOnAndConnected(this)) {

                Log.e("DataUploadService", "Wifi connected, uploading data...");

                File folder = new File(getFilesDir().getAbsolutePath() + "/SensorData/data/");
                File fileList[] = folder.listFiles();
                if (fileList.length == 0) {
                    prefs.setIsDataUploadPending(false);
                    stopSelf();
                } else {
                    for (final File child : fileList) {
                        final String fileKey = child.getName();
                        TransferObserver observer = uploadToAWS(fileKey, child);

                        transfers.add(observer.getId());

                        observer.setTransferListener(new TransferListener(){

                            @Override
                            public void onStateChanged(int id, TransferState state) {
                                if (state.equals(TransferState.COMPLETED)) {
                                    Log.e("DataUploadService", fileKey + " uploaded");
                                    child.delete();
                                    transfers.remove((Object) id);
                                    if (transfers.isEmpty()) {
                                        prefs.setIsDataUploadPending(false);
                                        AlarmHelper.get().resetSensingAlarmsAfterSensing(prefs.getSensingEndTime());
                                        stopSelf();
                                    }
                                } else if (state.equals(TransferState.FAILED)) {
                                    Crashlytics.log("Data transfer error state: FAILED");
                                } else if (state.equals(TransferState.UNKNOWN)) {
                                    Crashlytics.log("Data transfer error state: UNKNOWN");
                                }
                            }

                            @Override
                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                //int percentage = (int) (bytesCurrent/bytesTotal * 100);
                                //Display percentage transfered to user
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                Crashlytics.logException(ex);
                                setSelfAlarm(DataUpload.class);
                                stopSelf();
                            }

                        });

                    }
                }

            } else {
                setSelfAlarm(DataUpload.class);
                stopSelf();
            }
        }
        else {
            Log.e("DataUploadService", "No data awaiting upload");
            stopSelf();
        }
    }

    @Override
    protected void setSelfAlarm(Class classType) {
        Log.e("Databupoload", "", new RuntimeException("HEre 2"));
        AlarmHelper.get().setUploadRetryAlarm(classType);



    }
}
