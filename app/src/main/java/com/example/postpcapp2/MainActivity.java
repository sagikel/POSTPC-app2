package com.example.postpcapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    LocationTracker locationTracker;
    ImageButton startButton;
    ImageButton stopButton;
    TextView textViewHome;
    Button buttonSet;
    Button buttonClear;
    TextView textViewLat;
    TextView textViewLon;
    TextView textViewAc;

    SharedPreferences sharedPreferences;
    LocationInfo locationInfo;
    BroadcastReceiver broadcastReceiver;

    double lat;
    double lon;
    float ac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.imageButton1);
        stopButton = findViewById(R.id.imageButton2);
        textViewHome = findViewById(R.id.textViewHome);
        buttonSet = findViewById(R.id.buttonSet);
        buttonClear = findViewById(R.id.buttonClear);
        textViewLat = findViewById(R.id.textViewLat);
        textViewLon = findViewById(R.id.textViewLon);
        textViewAc = findViewById(R.id.textViewAc);

        sharedPreferences = getSharedPreferences("SP", MODE_PRIVATE);
        loudLocation();

        locationTracker = new LocationTracker(this, this);

        broadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter("LocationTracker.new_location");
        filter.addAction("LocationTracker.start");
        filter.addAction("LocationTracker.stop");
        registerReceiver(broadcastReceiver, filter);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTimePermissions();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareToStopTrack();
            }
        });
        buttonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLocation();
            }
        });
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLocation();
            }
        });

    }

    private void runTimePermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        } else {
            prepareToTrack();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                prepareToTrack();
            }
            else {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("We need this permission or we can't operate..")
                        .setIcon(R.drawable.alert_per)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton("still NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {} })
                        .create()
                        .show();
            }
        }
    }

    private void prepareToTrack() {
        startButton.setVisibility(View.INVISIBLE);
        stopButton.setVisibility(View.VISIBLE);
        textViewLat.setVisibility(View.VISIBLE);
        textViewLon.setVisibility(View.VISIBLE);
        textViewAc.setVisibility(View.VISIBLE);

        // start track
        locationTracker.startLocationUpdates();
    }

    private void prepareToStopTrack() {
        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.INVISIBLE);
        textViewLat.setVisibility(View.INVISIBLE);
        textViewLon.setVisibility(View.INVISIBLE);
        textViewAc.setVisibility(View.INVISIBLE);

        // stop track
        locationTracker.stopLocationUpdates();
        buttonSet.setVisibility(View.INVISIBLE);
    }

    public void setHomeButton(){
        if (ac < 50.0) {
            buttonSet.setVisibility(View.VISIBLE);
        }
        else {
            buttonSet.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setHomeText(){
        textViewHome.setText("Your home location is defined as " +
                locationInfo.getLatitude() + ", " +
                locationInfo.getLongitude());
    }

    private void saveLocation() {
        locationInfo = new LocationInfo(lat, lon, ac);
        setHomeText();
        buttonClear.setVisibility(View.VISIBLE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(locationInfo);
        editor.putString("Home location", json);
        editor.apply();
    }

    private void loudLocation() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Home location", null);
        if (json == null)
            return;
        locationInfo = gson.fromJson(json, LocationInfo.class);
        if (locationInfo.getAccuracy() == -1)
            return;
        setHomeText();
        buttonClear.setVisibility(View.VISIBLE);
    }

    private void deleteLocation() {
        locationInfo.setAccuracy(-1);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(locationInfo);
        editor.putString("Home location", json);
        editor.apply();

        textViewHome.setText("");
        buttonClear.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTracker.stopLocationUpdates();
        unregisterReceiver(broadcastReceiver);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())){
                case "LocationTracker.start":
                    //Toast.makeText(context, "start tracking", Toast.LENGTH_SHORT).show();
                    break;
                case "LocationTracker.new_location":
                    lat = intent.getDoubleExtra("textViewLat", 0);
                    textViewLat.setText(String.valueOf(lat));
                    lon = intent.getDoubleExtra("textViewLon", 0);
                    textViewLon.setText(String.valueOf(lon));
                    ac = intent.getFloatExtra("textViewAc", 50);
                    textViewAc.setText(String.valueOf(ac));
                    setHomeButton();
                    break;
                case "LocationTracker.stop":
                    //Toast.makeText(context, "stop tracking", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
