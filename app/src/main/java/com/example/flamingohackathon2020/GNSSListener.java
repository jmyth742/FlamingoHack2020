package com.example.flamingohackathon2020;

import android.util.Log;
import android.util.Pair;


import androidx.appcompat.app.AppCompatActivity;

import flamingo.flamingo_api.FlamingoLocation;
import flamingo.flamingo_api.FlamingoLocationListener;
import flamingo.flamingo_api.utils.ReferenceStationStatus;

public class GNSSListener extends AppCompatActivity implements FlamingoLocationListener {

    private double latitude = 0.0f;
    private double longitude = 0.0f;
    Camera mContext;

    boolean locationReady = false;

    @Override
    public void registerFlamingoLocationListener() {

        Log.v("Flamingo","registered");
    }

    @Override
    public void unregisterFlamingoLocationListener() {

        Log.v("Flamingo","unregistered");

    }

    @Override
    public void onFlamingoLocation(FlamingoLocation flamingoLocation) {
        Log.v("Flamingo","received info");
        latitude = flamingoLocation.getLatitude();
        longitude = flamingoLocation.getLongitude();
        locationReady = true;
        String text = "new pair of location data lat: " + String.valueOf(latitude) + " lon: " + String.valueOf(longitude);
        Log.v("Flamingo",text);
    }

    @Override
    public void onReferenceStationStatusChanged(ReferenceStationStatus referenceStationStatus) {

    }

    public Pair<Double, Double> getLocation(){

        if(locationReady){
            locationReady = false;
            String text = "new pair of location data lat: " + latitude + " lon: " + longitude;
            Log.v("Flamingo",text);
            return new Pair<>(latitude,longitude);
        }
        else{
            String text = "No new pair of location data available";
                    Log.v("Flamingo",text);

        }

        return new Pair<>(0.0,0.0);

    }
}
