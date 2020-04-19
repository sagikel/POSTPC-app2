package com.example.postpcapp2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class LocalSendSmsBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "Channel";
    public final String PHONE = "PHONE";
    public final String CONTENT = "CONTENT";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("POST_PC.ACTION_SEND_SMS")) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("LocalSendSmsBroadcastR", "SEND SMS permission not granted");
                return;
            }
            String number = intent.getStringExtra(PHONE);
            String msg = intent.getStringExtra(CONTENT);
            if (number == null || number.equals("") || msg == null || msg.equals("")) {
                Log.e("LocalSendSmsBroadcastR", "no Extra or blank Extra in intent");
                return;
            }
            SmsManager.getDefault().sendTextMessage(
                    number,
                    null,
                    msg,
                    null,
                    null);

            CharSequence message = "Sending sms to " + number + ":\n" + msg;
            NotificationCompat.Builder builder = new
                    NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.alert_sms)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentTitle("SMS sending")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            int notificationId = 1;
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
