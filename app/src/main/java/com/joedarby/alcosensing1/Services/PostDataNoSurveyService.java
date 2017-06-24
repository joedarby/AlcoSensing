package com.joedarby.alcosensing1.Services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.joedarby.alcosensing1.Data.AppPrefs;
import com.joedarby.alcosensing1.Data.SurveyData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PostDataNoSurveyService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);

        AppPrefs prefs = AppPrefs.getInstance(this);
        prefs.incrementResponseCount();

        SurveyData data = new SurveyData(this);
        String fileName = prefs.getUserID() + "-" + prefs.getSensingStartTime() + "-";

        prefs.setIsSurveyPending(false);

        Gson gson = new Gson();
        File file = new File(getFilesDir().getAbsolutePath() + "/SensorData/data/", fileName + "SurveyResult.json");

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(gson.toJson(data).getBytes());
            stream.close();
        } catch (IOException e) {
            Log.e("Survey File", "Not created", e);
        }

        prefs.setIsDataUploadPending(true);

        Intent dataUploadIntent = DataUpload.getIntent();
        startService(dataUploadIntent);

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
