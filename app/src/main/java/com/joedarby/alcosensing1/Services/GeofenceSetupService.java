package com.joedarby.alcosensing1.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.joedarby.alcosensing1.Data.AppPrefs;
import com.joedarby.alcosensing1.Helpers.AlarmHelper;


public class GeofenceSetupService extends Service {
    private FusedLocationProviderClient mFusedLocationClient;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private static final int RADIUS = 100; //metres
    private static final long EXPIRATION_MILLIS = Geofence.NEVER_EXPIRE;
    private static final String tag = "GeofenceSetupService";
    private static final String REQUEST_ID = "current_location";
    private final Handler handler = new Handler();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location == null) {
                                Log.e(tag, "Location null");
                                restartService();
                            } else {
                                double lat = location.getLatitude();
                                double lng = location.getLongitude();
                                Log.e(tag, "Geofence to be added " + lat + " " + lng);
                                GeofencingRequest geofencingRequest = getGeofencingRequest(lat, lng);
                                registerGeofence(geofencingRequest);
                                stopSelf();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(tag, "Fused location fail. Restarting in 5 seconds.");
                            restartService();
                        }
                    });
        } catch (SecurityException e) {
            Log.e(tag, "Loc Permission not avail");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private GeofencingRequest getGeofencingRequest(double lat, double lng) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(REQUEST_ID)
                .setCircularRegion(lat, lng, RADIUS)
                .setExpirationDuration(EXPIRATION_MILLIS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest.Builder requestBuilder = new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        return requestBuilder.build();
    }

    private void registerGeofence(GeofencingRequest request) {
        if (mGeofencePendingIntent == null) {
            Intent intent = new Intent(this, GeofenceReceiver.class);
            intent.setAction("com.joedarby.alcosensing.Services.GeofenceReceiver.ACTION_RECEIVE_GEOFENCE");
            mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        try {
            mGeofencingClient.removeGeofences(mGeofencePendingIntent);
            mGeofencingClient.addGeofences(request, mGeofencePendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(tag, "Geofence added");

                    }
            })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(tag, "Geofence adding failed");
                        restartService();
                    }
                });
        } catch (SecurityException e) {
            Log.e(tag, "Loc Permission not avail");
        }
    }

    private void restartService() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlarmHelper.get().resetGeofence(AppPrefs.getInstance(GeofenceSetupService.this).getSensingEndTime(), true);
                stopSelf();
            }
        }, 1000*5);
    }
}
