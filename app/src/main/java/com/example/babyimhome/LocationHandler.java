package com.example.babyimhome;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHandler {
    public static final String INTENT_SEND = "sending location message";
    public static final String TRACKING = "tracking";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ACCURACY = "accuracy";
    private static final int FAST_REFRESH_TIME = 5000;
    public static final int NORMAL_REFRESH_TIME = 10000;


    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean flag = true;
    private double currLatitude;
    private double currLongitude;

    public double getLatitude() {
        return currLatitude;
    }

    public double getLongitude() {
        return currLongitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    private float accuracy;

    public LocationHandler(Context context) {
        this.context = context;
        this.fusedLocationProviderClient = new FusedLocationProviderClient(context);
        locationRequest = new LocationRequest();
        locationCallback = initLocationCallback();

    }

    public void startTracking() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(NORMAL_REFRESH_TIME);
        locationRequest.setFastestInterval(FAST_REFRESH_TIME);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(context);
        client.checkLocationSettings(builder.build()).addOnSuccessListener((MainActivity)context,
                new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("done", e.getMessage());

            }
        });
    }


    public void stopTracking() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,null);
    }

    private LocationCallback initLocationCallback() {
        LocationCallback locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Intent intent = new Intent();
                intent.setAction(INTENT_SEND);
                intent.putExtra(TRACKING, flag);
                if (locationResult == null) {
                    intent.putExtra(TRACKING, flag);
                    context.sendBroadcast(intent);
                    return;
                } else {
                    for (Location location : locationResult.getLocations()) {
                        currLatitude = location.getLatitude();
                        currLongitude = location.getLongitude();
                        accuracy = location.getAccuracy();
                        intent.putExtra(LATITUDE,location.getLatitude());
                        intent.putExtra(LONGITUDE,location.getLongitude());
                        Log.i("new accuracy: ", String.valueOf(location.getAccuracy()));
                        intent.putExtra(ACCURACY,location.getAccuracy());
                        intent.putExtra(TRACKING,flag);
                    }
                    context.sendBroadcast(intent);
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };
        return locationCallback;
    }

}
