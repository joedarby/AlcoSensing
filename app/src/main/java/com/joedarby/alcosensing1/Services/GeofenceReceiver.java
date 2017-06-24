package com.joedarby.alcosensing1.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


public class GeofenceReceiver extends BroadcastReceiver {
    private static final String tag = "LocChangeIS";


    public GeofenceReceiver() {


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(tag, "Geofence Intent Received");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(tag, String.valueOf(geofencingEvent.getErrorCode()));
            Intent newGeofenceIntent = new Intent(context, GeofenceSetupService.class);
            context.startService(newGeofenceIntent);
        } else {
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.e(tag, "Geofence Exit Detected");
                Intent placesIntent = new Intent(context, GeofenceResultService.class);
                context.startService(placesIntent);

            } else {
                Log.e(tag, "Erroneous geofence transition");
                Intent newGeofenceIntent = new Intent(context, GeofenceSetupService.class);
                context.startService(newGeofenceIntent);
            }
        }
    }

}
