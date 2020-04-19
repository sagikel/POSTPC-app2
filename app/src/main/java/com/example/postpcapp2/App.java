package com.example.postpcapp2;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.os.Build;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class App extends Application {

    public LocalSendSmsBroadcastReceiver localSendSmsBroadcastReceiver;
    PeriodicWorkRequest periodicWorkRequest;

    @Override
    public void onCreate() {
        super.onCreate();

        localSendSmsBroadcastReceiver = new LocalSendSmsBroadcastReceiver();
        IntentFilter filter = new IntentFilter("POST_PC.ACTION_SEND_SMS");
        registerReceiver(localSendSmsBroadcastReceiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Channel", "Honey", importance);
            channel.setDescription("Honey app");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        periodicWorkRequest = new PeriodicWorkRequest.Builder(
                TrackerWorker.class,
                15,
                TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("TRACK_AND_SMS",
                        ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
    }
}
