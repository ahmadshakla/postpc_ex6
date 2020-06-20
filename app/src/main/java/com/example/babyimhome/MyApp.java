package com.example.babyimhome;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MyApp extends Application {
    private final static String ACTION = "POST_PC.ACTION_SEND_SMS";
    @Override
    public void onCreate() {
        super.onCreate();
        BroadcastReceiver broadcastReceiver = new LocalSendSmsBroadcastReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION));
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SMSLocationWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .build();
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(request);
    }
}
