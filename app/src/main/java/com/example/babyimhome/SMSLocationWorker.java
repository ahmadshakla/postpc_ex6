package com.example.babyimhome;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.ActivityCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

public class SMSLocationWorker extends ListenableWorker {
    public static final int UNRECEIVED_INPUT = -1;
    public static final int MIN_ACCURACY = 50;
    private Context mainContext;
    private LocationHandler locationHandler;
    private CallbackToFutureAdapter.Completer<Result> futureCallback;
    private BroadcastReceiver broadcastReceiver;
    private double oldLatitude, oldLongitude;
    private String phoneNum;
    private SharedPreferences sharedPreferences;



    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public SMSLocationWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.mainContext = appContext;
        this.locationHandler = new LocationHandler(mainContext);
        futureCallback = null;
        broadcastReceiver = null;
        sharedPreferences = mainContext.getSharedPreferences(MainActivity.SHARED_PREF, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        SharedPreferences sp = mainContext.getSharedPreferences(MainActivity.SHARED_PREF, Context.MODE_PRIVATE);
        ListenableFuture<Result> future = CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull CallbackToFutureAdapter.Completer<Result> completer) throws Exception {
                futureCallback = completer;
                return null;
            }

        });
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                float accuracy = intent.getFloatExtra(LocationHandler.ACCURACY,0f);
                if (accuracy != 0f && accuracy <=MIN_ACCURACY){
                    handleReceivedBroadcast();
                }
            }
        };
        mainContext.registerReceiver(broadcastReceiver, new IntentFilter(LocationHandler.INTENT_SEND));
        boolean smsGranted = ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED;
        boolean locationGranted = ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean permissionGranted = smsGranted && locationGranted;
        if (!permissionGranted) {
            futureCallback.set(Result.success());
        }
        oldLatitude = sp.getFloat(LocationHandler.LATITUDE, UNRECEIVED_INPUT);
        oldLongitude = sp.getFloat(LocationHandler.LONGITUDE, UNRECEIVED_INPUT);
        phoneNum = sp.getString(LocalSendSmsBroadcastReceiver.PHONE, null);
        if (oldLatitude == UNRECEIVED_INPUT || oldLongitude == UNRECEIVED_INPUT || phoneNum == null || phoneNum.length()==0) {
            futureCallback.set(Result.success());
        }
        else {
            locationHandler.startTracking();
        }
        return future;

    }

    private void handleReceivedBroadcast() {
        float[] distance = new float[1];
        mainContext.unregisterReceiver(broadcastReceiver);
        double curLat = locationHandler.getLatitude();
        double curLon = locationHandler.getLongitude();
        locationHandler.stopTracking();
        double prevLat = sharedPreferences.getFloat(LocationHandler.LATITUDE, UNRECEIVED_INPUT);
        double prevLon = sharedPreferences.getFloat(LocationHandler.LONGITUDE, UNRECEIVED_INPUT);
        Location.distanceBetween(prevLat, prevLon, curLat, curLon, distance);
        sharedPreferences.edit().putFloat(LocationHandler.LATITUDE,(float) curLat).putFloat(LocationHandler.LONGITUDE,(float)curLon).apply();
        if (prevLat == UNRECEIVED_INPUT || distance[0] < MIN_ACCURACY) {
            futureCallback.set(Result.success());
            return;
        }
        Location.distanceBetween(curLat, curLon, oldLatitude, oldLongitude, distance);
        if (distance[0] < MIN_ACCURACY) {
            Intent intent = new Intent();
            setIntent(intent);
        }
        futureCallback.set(Result.success());
    }

    private void setIntent(Intent intent){
        intent.setAction(MainActivity.ACTION);
        intent.putExtra(LocalSendSmsBroadcastReceiver.PHONE, phoneNum);
        intent.putExtra(LocalSendSmsBroadcastReceiver.CONTENT, "Honey, I'm home");
        mainContext.sendBroadcast(intent);
    }
}
