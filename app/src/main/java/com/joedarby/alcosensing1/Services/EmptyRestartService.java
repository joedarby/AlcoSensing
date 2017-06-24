package com.joedarby.alcosensing1.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.joedarby.alcosensing1.BackboneApplication;

//Empty Service just to facilitate restart
public class EmptyRestartService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("EmptyRestartService", "Restarted");
        BackboneApplication.appRecoverySanityChecks(this);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
