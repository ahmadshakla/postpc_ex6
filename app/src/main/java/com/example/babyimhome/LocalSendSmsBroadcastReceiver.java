package com.example.babyimhome;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.core.app.ActivityCompat;

public class LocalSendSmsBroadcastReceiver extends BroadcastReceiver {
    public static final String PHONE = "phoneNumber";
    public static final String CONTENT = "msgContent";
    public static final String SMS_SENT = "sendingSMS";
    public static final String SMS_DELIVERED = "deliveredSMS";
    public static final int MSG_REQUEST_CODE = 14;

    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNum = intent.getStringExtra(PHONE);
        String msgContent = intent.getStringExtra(CONTENT);
        sendSMS(context, phoneNum, msgContent);
        NotificationHandler notificationHandler = new NotificationHandler(phoneNum, msgContent, context);
        notificationHandler.createNotificationIfNotExists();
        notificationHandler.showNotification();

    }


    public void sendSMS(Context context, String phoneNum, String content) {
        PendingIntent piSend = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), 0);
        SmsManager.getDefault().sendTextMessage(phoneNum, null, content, piSend, piDelivered);

    }
}
