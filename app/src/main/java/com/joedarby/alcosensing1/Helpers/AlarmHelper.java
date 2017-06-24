package com.joedarby.alcosensing1.Helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.joedarby.alcosensing1.BackboneApplication;
import com.joedarby.alcosensing1.Services.EmptyRestartService;
import com.joedarby.alcosensing1.Services.GeofenceSetupService;
import com.joedarby.alcosensing1.Services.SensingTriggerService;
import com.joedarby.alcosensing1.Services.SurveyTriggerService;

import java.util.Date;

public class AlarmHelper {
    private static AlarmHelper instance;

    private final AlarmManager alarmManager;
    private final Context context;
    private static final String tag = "AlarmHelper";

    private AlarmHelper(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
    }

    public static AlarmHelper get() {
        if (instance == null) {
            instance = new AlarmHelper(BackboneApplication.getContext());
        }
        return instance;
    }

    public void resetSensingAlarmsAfterSensing(Long lastSensingEndTime) {

        long geofencingStartTime = lastSensingEndTime + TimeConstants.GEOFENCING_RESET_DELAY_AFTER_SENSING;
        long periodicAttemptStartTime = lastSensingEndTime + TimeConstants.PERIOD_ALARM_RESET_DELAY;

        Intent geofencingIntent = new Intent(context, GeofenceSetupService.class);
        PendingIntent geofencingPendingIntent = PendingIntent.getService(context, 0, geofencingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(geofencingPendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, geofencingStartTime, geofencingPendingIntent);
        Log.e(tag, String.format("geofence alarm set for %d", geofencingStartTime));

        Intent sensingTriggerIntent = new Intent(context, SensingTriggerService.class);
        sensingTriggerIntent.putExtra("UserTriggered", false);
        PendingIntent pendingSensingIntent = PendingIntent.getService(context, 0, sensingTriggerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingSensingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, periodicAttemptStartTime, TimeConstants.PERIODIC_SENSING_ATTEMPT_INTERVAL, pendingSensingIntent);
        Log.e(tag, String.format("repeating alarm set starting %d", periodicAttemptStartTime));
    }

    public void resetGeofence(Long lastSensingEndTime, boolean previousSetupFailed) {
        long geofencingStartTime;
        if (previousSetupFailed) {
            geofencingStartTime = new Date().getTime() + TimeConstants.GEOFENCING_RESET_DELAY_AFTER_FAILURE;
        } else {
            geofencingStartTime = lastSensingEndTime + TimeConstants.GEOFENCING_RESET_DELAY_AFTER_SENSING;
        }
        Intent geofencingIntent = new Intent(context, GeofenceSetupService.class);
        PendingIntent geofencingPendingIntent = PendingIntent.getService(context, 0, geofencingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(geofencingPendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, geofencingStartTime, geofencingPendingIntent);
    }

    public void setUploadRetryAlarm(Class classType) {
        Intent selfIntent = new Intent(context, classType);
        PendingIntent selfPendingIntent = PendingIntent.getService(context, 0, selfIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long setTime = (new Date()).getTime() + TimeConstants.DATA_UPLOAD_RETRY_DELAY;
        alarmManager.cancel(selfPendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, setTime, selfPendingIntent);
    }

    public void setSurveyAlarm(Long scheduleTime) {
        Log.e("AlarmHelper", "Setting alarm");
        Intent surveyTriggerIntent = new Intent(context, SurveyTriggerService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, surveyTriggerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleTime, pendingIntent);
    }

    public void setEmptyRestartAlarm() {
        Intent intent = new Intent(context, EmptyRestartService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getService(BackboneApplication.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
    }

    public void userAbortedSensingReset(Long unblockTime) {
        Intent sensingTriggerIntent = new Intent(context, SensingTriggerService.class);
        sensingTriggerIntent.putExtra("UserTriggered", false);
        PendingIntent pendingSensingIntent = PendingIntent.getService(context, 0, sensingTriggerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingSensingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, unblockTime, TimeConstants.PERIODIC_SENSING_ATTEMPT_INTERVAL, pendingSensingIntent);
        Log.e(tag, "repeating alarm reset after abort");

        Intent geofencingIntent = new Intent(context, GeofenceSetupService.class);
        PendingIntent geofencingPendingIntent = PendingIntent.getService(context, 0, geofencingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(geofencingPendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, unblockTime, geofencingPendingIntent);
        Log.e(tag, "geofence reset after abort");
    }

    public void cancelRepeatingAlarm() {
        Intent sensingTriggerIntent = new Intent(context, SensingTriggerService.class);
        sensingTriggerIntent.putExtra("UserTriggered", false);
        PendingIntent pendingSensingIntent = PendingIntent.getService(context, 0, sensingTriggerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingSensingIntent);
    }
}
