package com.example.postpcapp2;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationTracker {

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public LocationTracker(final Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    Intent intent = new Intent();
                    intent.setAction("LocationTracker.new_location");
                    intent.putExtra("textViewLat", location.getLatitude());
                    intent.putExtra("textViewLon", location.getLongitude());
                    intent.putExtra("textViewAc", location.getAccuracy());
                    context.sendBroadcast(intent);
                }
            }
        };
    }

    public void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());

        Intent intent = new Intent();
        intent.setAction("LocationTracker.start");
        context.sendBroadcast(intent);
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);

        Intent intent = new Intent();
        intent.setAction("LocationTracker.stop");
        context.sendBroadcast(intent);
    }
}
