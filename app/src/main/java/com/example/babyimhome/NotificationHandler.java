package com.example.babyimhome;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHandler {

    private static final int NOTIFICATION_ID = 34;
    private static final String CHANNEL_ID = "POST_PC_EX_6";
    private static final String NOTIFICATION_NAME = "message notification";
    private static final String DESCRIPTION = "Sending SMS to ";

    private String phoneNum;
    private String content;
    private Context context;

    public NotificationHandler(String phoneNum, String content, Context context) {
        this.phoneNum = phoneNum;
        this.content = content;
        this.context = context;
    }

    public void createNotificationIfNotExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NOTIFICATION_NAME, importance);
            channel.setDescription(NOTIFICATION_NAME);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void showNotification() {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(DESCRIPTION + phoneNum + ": " + content)
                .setContentTitle(NOTIFICATION_NAME)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

}
