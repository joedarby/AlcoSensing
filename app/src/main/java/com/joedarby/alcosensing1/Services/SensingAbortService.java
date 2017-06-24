package com.joedarby.alcosensing1.Services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.joedarby.alcosensing1.Helpers.AlarmHelper;

import java.util.Calendar;


public class SensingAbortService extends Service {

    private ContinuousSensingService mService;

    private ServiceConnection mConnection;

    private Boolean restart;
    private Boolean batteryLow;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        restart = intent != null ? intent.getBooleanExtra("restart", false) : false;
        batteryLow = intent != null ? intent.getBooleanExtra("battery", false) : false;

        if (!restart && !batteryLow) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 3);
            Long unblockTime = cal.getTimeInMillis();

            resetAlarm(unblockTime);
        }

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ContinuousSensingService.LocalBinder binder = (ContinuousSensingService.LocalBinder) service;
                mService = binder.getService();
                mService.abortSensing(restart, batteryLow);
                stopSelf();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent targetIntent = new Intent(this, ContinuousSensingService.class);
        bindService(targetIntent, mConnection, Context.BIND_AUTO_CREATE);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    private void resetAlarm(Long unblockTime) {
        AlarmHelper.get().userAbortedSensingReset(unblockTime);
        Log.i("SensingAbortService", "Alarm reset for: " + unblockTime);
    }
}
