package com.joedarby.alcosensing1.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.joedarby.alcosensing1.Data.AppPrefs;
import com.joedarby.alcosensing1.Helpers.AlarmHelper;
import com.joedarby.alcosensing1.Helpers.StatusHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

public class SensingTriggerService extends Service {

    private AppPrefs prefs;
    private boolean mUserTriggered;
    private final String tag = "SensingTrigServ";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = AppPrefs.getInstance(this);
        mUserTriggered = intent.getBooleanExtra("UserTriggered", false);

        if (mUserTriggered) {
            if (StatusHelper.isSensingServiceRunning(this)) {
                Intent restartIntent = new Intent(this, SensingAbortService.class);
                restartIntent.putExtra("restart", true);
                startService(restartIntent);
                stopSelf();
            }
            else {
                triggerSensing();
            }
        } else  {
            if (StatusHelper.isSensingServiceRunning(this)) {
                Log.e(tag, "Sensing already, not user triggered so not restarting");
                stopSelf();
            } else {
                triggerSensing();
            }
        }
        return START_STICKY;
    }

    private void triggerSensing() {
        if (!prefs.isSurveyPending()
                && !prefs.isDataUploadPending()
                && (StatusHelper.getBatteryLevel(this) == StatusHelper.BATTERY_OK)
                && (mUserTriggered || sensingRandomizer())
                && prefs.isAppVersionOK()) {

            prefs.setSensingTriggeredByUser(mUserTriggered);
            Log.e(tag, "Triggering sensing.");

            Intent sensingIntent = new Intent(this, ContinuousSensingService.class);
            startService(sensingIntent);
            AlarmHelper.get().cancelRepeatingAlarm();
        }
        else {
            Log.e(tag, "Sensing not triggered.");
            AlarmHelper.get().resetGeofence(prefs.getSensingEndTime(), false);
        }
        stopSelf();
    }

    private boolean sensingRandomizer() {
        int randomInt = new Random().nextInt(100);


        Calendar now = GregorianCalendar.getInstance();
        int day = now.get(Calendar.DAY_OF_WEEK);
        int hour = now.get(Calendar.HOUR_OF_DAY);

        Log.e(tag, String.format("Random number is %d, day is %d, hour is %d", randomInt, day, hour));

        //If between 1am and 9 am, never start sensing
        if (hour >= 1 && hour < 9) {
            Log.e(tag, "0% chance of sensing");
            return false;
        }
        //If friday/saturday night between 10pm and 1am, 80% chance of triggering sensing
         else if (((day == Calendar.FRIDAY || day == Calendar.SATURDAY) && hour >= 22)
                || ((day == Calendar.SATURDAY || day == Calendar.SUNDAY) && hour < 1)) {
            Log.e(tag, "80% chance of sensing");
            return (randomInt >= 20);
        }
        //If friday/saturday between 5pm and 10pm 30% chance of triggering sensing
        else if ((day == Calendar.FRIDAY || day == Calendar.SATURDAY) && hour >= 17) {
            Log.e(tag, "30% chance of sensing");
            return (randomInt >= 70);
        }
        //If any other day between 5pm and 10pm 10% chance of triggering sensing
        else if (hour >= 17 && hour < 22) {
            Log.e(tag, "10% chance of sensing");
            return (randomInt >= 90);
        }
        //Otherwise 5% chance
        else {
            Log.e(tag, "5% chance of sensing");
            return (randomInt >= 95);
        }

    }

}
