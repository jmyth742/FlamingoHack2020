package com.example.flamingohackathon2020;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

import flamingo.flamingo_api.FlamingoLocation;
import flamingo.flamingo_api.FlamingoLocationListener;
import flamingo.flamingo_api.utils.ReferenceStationStatus;

public class GNSSListener extends AppCompatActivity implements FlamingoLocationListener, Serializable {

    private double latitude = 0.0f;
    private double longitude = 0.0f;
    Context mContext;
    GoogleMap map = null;

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
        showToast("received position");

        runOnUiThread(() -> updateMap());

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

    public void showToast(final String toast)
    {
        runOnUiThread(() -> Toast.makeText(mContext.getApplicationContext(), toast, Toast.LENGTH_SHORT).show());
    }


    void updateMap(){
        if(map != null){
            LatLng bln = new LatLng(latitude, longitude);
            MarkerOptions marker = new MarkerOptions();
            marker.title("Marker in Berlin");
            marker.position(bln);
            marker.snippet("");
            map.addMarker(marker);
            map.moveCamera(CameraUpdateFactory.newLatLng(bln));
        }
    }
}
