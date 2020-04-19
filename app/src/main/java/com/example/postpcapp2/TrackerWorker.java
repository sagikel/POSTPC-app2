package com.example.postpcapp2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

public class TrackerWorker extends Worker {

    private LocationInfo locationInfo;
    private LocationInfo lastLocationHome;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private String number;
    private BroadcastReceiver broadcastReceiver;
    private LocationTracker locationTracker;

    public TrackerWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sharedPreferences = getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    @Override
    public Result doWork() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return Result.success();
        }

        String json = sharedPreferences.getString("Home location", null);
        if (json == null)
            return Result.success();
        lastLocationHome = gson.fromJson(json, LocationInfo.class);
        if (lastLocationHome.getAccuracy() == -1)
            return Result.success();
        json = sharedPreferences.getString("Number SMS", null);
        if (json == null) {
            number = "";
            return Result.success();
        }
        number = gson.fromJson(json, String.class);
        if (number.equals(""))
            return Result.success();

        broadcastReceiver = new TrackerWorker.MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter("LocationTracker.new_location");
        getApplicationContext().registerReceiver(broadcastReceiver, filter);
        locationTracker = new LocationTracker(getApplicationContext());
        locationTracker.startLocationUpdates();
        return Result.success();
    }

    private void checkConditions() {
        String json = sharedPreferences.getString("last location", null);
        saveLastLocation();
        if (json == null) {
            return;
        }
        LocationInfo lastLocationInfo = gson.fromJson(json, LocationInfo.class);
        double dis1 = dis(lastLocationInfo, locationInfo);
        if (dis1 < 50) {
            return;
        }
        double dis2 = dis(lastLocationHome, locationInfo);
        if (dis2 < 50) {
            sendSMS();
        }
    }

    private void sendSMS() {
        App app = (App) getApplicationContext();
        Intent intent = new Intent();
        intent.setAction("POST_PC.ACTION_SEND_SMS");
        intent.putExtra(app.localSendSmsBroadcastReceiver.PHONE, number);
        intent.putExtra(app.localSendSmsBroadcastReceiver.CONTENT,
                "Honey I'm Home!");
        app.sendBroadcast(intent);
    }

    private void saveLastLocation(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(locationInfo);
        editor.putString("last location", json);
        editor.apply();
    }

    private double dis(LocationInfo A, LocationInfo B) {
        Location locationA = new Location("point A");
        locationA.setLatitude(A.getLatitude());
        locationA.setLongitude(A.getLongitude());
        Location locationB = new Location("point B");
        locationB.setLatitude(B.getLatitude());
        locationB.setLongitude(B.getLongitude());

        return locationA.distanceTo(locationB);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("LocationTracker.new_location".equals(intent.getAction())) {
                float accuracy = intent.getFloatExtra("textViewAc", 50);
                if (accuracy < 50) {
                    locationTracker.stopLocationUpdates();
                    getApplicationContext().unregisterReceiver(broadcastReceiver);
                    locationInfo = new LocationInfo(
                            intent.getDoubleExtra("textViewLat", 0),
                            intent.getDoubleExtra("textViewLon", 0),
                            accuracy);
                    checkConditions();
                }
            }
        }
    }
}