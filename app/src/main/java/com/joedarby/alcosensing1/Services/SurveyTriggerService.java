package com.joedarby.alcosensing1.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.joedarby.alcosensing1.Data.AppPrefs;
import com.joedarby.alcosensing1.Data.PostDataSurveyActivity;
import com.joedarby.alcosensing1.Data.YesNoDialogActivity;
import com.joedarby.alcosensing1.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SurveyTriggerService extends Service {

    private NotificationManager nManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        showSurveyNotification();
        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }


    private void showSurveyNotification() {
        AppPrefs prefs = AppPrefs.getInstance(this);
        String startString = prefs.getSensingStartTime();
        String start = startString.substring(startString.length()-5);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String end = format.format(new Date(prefs.getSensingEndTime()));

        Intent yesNoIntent = new Intent(this, YesNoDialogActivity.class);
        yesNoIntent.putExtra("start", start);
        yesNoIntent.putExtra("end", end);
        PendingIntent yesNoPendingIntent = PendingIntent.getActivity(this, 0, yesNoIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent surveyIntent = new Intent(this, PostDataSurveyActivity.class);
        surveyIntent.putExtra("start", start);
        surveyIntent.putExtra("end", end);
        PendingIntent surveyPendingIntent = PendingIntent.getActivity(this, 0, surveyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent noSurveyIntent = new Intent(this, PostDataNoSurveyService.class);
        PendingIntent noSurveyPendingIntent = PendingIntent.getService(this,  0, noSurveyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] pattern = {600,600,600};

        Notification surveyNotification = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format("From %s until %s.\nDid you drink any alcohol between these times?", start, end)))
                .setContentTitle("AlcoSensing App Survey")
                .setSmallIcon(R.mipmap.pint_glass)
                .setWhen(System.currentTimeMillis() + 5000)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setVibrate(pattern)
                .setAutoCancel(false)
                .addAction(R.mipmap.pint_glass, "Yes", surveyPendingIntent)
                .addAction(R.mipmap.pint_glass, "No", noSurveyPendingIntent)
                .setContentIntent(yesNoPendingIntent)
                .build();

        surveyNotification.flags = Notification.FLAG_ONGOING_EVENT;


        nManager.notify(0, surveyNotification);

    }
}
