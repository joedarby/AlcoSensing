package com.joedarby.alcosensing1.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.joedarby.alcosensing1.BackboneApplication;


public class OnBootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equalsIgnoreCase(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            Log.e("OnBootBroadcastReceiver", intent.getAction());
            BackboneApplication.appRecoverySanityChecks(context);

        }
    }
}
