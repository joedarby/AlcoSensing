package com.joedarby.alcosensing1.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BatteryBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BatteryBroadcastRec", "Triggered");
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_LOW)) {
            Intent abortIntent = new Intent(context, SensingAbortService.class);
            abortIntent.putExtra("battery", true);
            context.startService(abortIntent);
            Log.e("BatteryBroadcastRec", "Unregistered");
        }
    }
}
