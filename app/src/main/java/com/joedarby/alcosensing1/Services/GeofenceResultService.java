package com.joedarby.alcosensing1.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.List;

public class GeofenceResultService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private final List<Integer> significantPlaceTypes = new ArrayList<>(300);
    private static final String tag = "GeoResultService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(tag, "started");
        GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(this);
        Intent geofenceIntent = new Intent(this, GeofenceReceiver.class);
        geofenceIntent.setAction("com.joedarby.alcosensing.Services.GeofenceReceiver.ACTION_RECEIVE_GEOFENCE");
        PendingIntent geofencePendingIntent = PendingIntent.getBroadcast(this, 0, geofenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mGeofencingClient.removeGeofences(geofencePendingIntent);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
    }

    private void checkPlace() {
        try {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    try {
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            Log.i("PlacesTestActivity", String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));

                            if (placeLikelihood.getLikelihood() > 0.4) {
                                List<Integer> placeTypes = (placeLikelihood.getPlace().getPlaceTypes());
                                significantPlaceTypes.addAll(placeTypes);
                            }
                        }
                        Place mostLikelyPlace = likelyPlaces.get(0).getPlace();
                        significantPlaceTypes.addAll(mostLikelyPlace.getPlaceTypes());
                        Log.i("PlacesTestActivity", String.format("Most likely place is: %s", mostLikelyPlace.getName()));
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        Crashlytics.logException(e);
                    }
                    if (significantPlaceTypes.contains(Place.TYPE_BAR)
                            || significantPlaceTypes.contains(Place.TYPE_NIGHT_CLUB)
                            || significantPlaceTypes.contains(Place.TYPE_RESTAURANT)) {
                        Log.e(tag, "Interesting location");
                        Intent sensingIntent = new Intent(GeofenceResultService.this, SensingTriggerService.class);
                        sensingIntent.putExtra("UserTriggered", false);
                        startService(sensingIntent);
                        significantPlaceTypes.clear();
                    } else {
                        significantPlaceTypes.clear();
                        Log.e(tag, "Not interesting location");
                        Intent newGeofenceIntent = new Intent(GeofenceResultService.this, GeofenceSetupService.class);
                        startService(newGeofenceIntent);
                    }
                    try {
                        likelyPlaces.release();
                    } catch (IllegalArgumentException e) {
                        Crashlytics.logException(e);
                    }
                    stopSelf();
                }
            });

        } catch (SecurityException e) {
            Log.e("GPlacesCheck", "Loc not available");
            stopSelf();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Intent newGeofenceIntent = new Intent(GeofenceResultService.this, GeofenceSetupService.class);
        startService(newGeofenceIntent);
        stopSelf();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPlace();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
