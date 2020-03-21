package com.example.flamingohackathon2020;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import flamingo.flamingo_api.FlamingoManager;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor acc, magno;

    String current_direction = "";


    float[] mGravity;
    float[] mGeomagnetic;
    float azimut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acc = sensorManager.getDefaultSensor((Sensor.TYPE_ACCELEROMETER));
        magno = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        assert sensorManager != null;


        Log.d("init", "activity started");


        setContentView(R.layout.activity_main);
    }


    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (sensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                // orientation contains azimut, pitch and roll
                float orientation[] = new float[3];
                sensorManager.getOrientation(R, orientation);
                azimut = orientation[0];

                String res = getDirection();
                Log.d("Compass", res);
            }
        }
    }

    private String getDirection(){
            if ((azimut > -0.2) && (azimut < 0.2)) current_direction = "North";
            if ((azimut > 1.3) && (azimut < 1.7)) current_direction = "East";
            if (((azimut < -2.8) && (azimut > -3.2)) || ((azimut < 3.2) && (azimut > 2.8))) current_direction = "South";
            if ((azimut > -1.7) && (azimut < -1.3)) current_direction = "West";
            return current_direction;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magno, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}
