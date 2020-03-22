package com.example.flamingohackathon2020

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import flamingo.flamingo_api.FlamingoLocationListener
import flamingo.flamingo_api.FlamingoManager
import flamingo.flamingo_api.utils.ReferenceStationStatus


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener {


    private lateinit var mMap: GoogleMap
    var tracker:GNSSListener = GNSSListener()
    var flamingoManager:FlamingoManager? = null

    var TAG: String = "Flamingo"

    val requestCode = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



    }

    fun setUpFlamingo(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // We do not have this permission. Let's ask the user
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),456);
        }

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode)


        val flamingoManager = FlamingoManager(this, arrayListOf(tracker))
        val applicationId = getString(R.string.applicationId)
        val password = getString(R.string.password)
        val companyId = getString(R.string.companyId)


        flamingoManager.addFlamingoListener(tracker)

        flamingoManager.registerFlamingoService(applicationId, password, companyId,tracker)

        Log.v(TAG,flamingoManager.positioningSession.toString())


        Log.v(TAG, "FLAMINGO REFERENCE STATION STATUS -> " + flamingoManager.referenceStationStatus.toString())
        Log.v(TAG, "FLAMINGO REGISTRATION STATION STATUS -> " + flamingoManager.registrationStatus.toString())

        var counter = 0
        while (flamingoManager.referenceStationStatus != ReferenceStationStatus.AVAILABLE && counter < 10){
            Log.v(TAG,"repeat")
            flamingoManager.registerFlamingoService(applicationId, password, companyId,tracker)
            Thread.sleep(500)
            counter += 1
        }

        //test the distance
        val new_coordinates = CoordinateFinder(52.52316261666667,13.422810166666666).newCoordinate(0.0,0.01)
        Log.v(TAG,"NEW COORDINATES -> " + new_coordinates.toString())

        this.flamingoManager = flamingoManager
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        tracker.map = mMap
        tracker.mContext = this

        setUpFlamingo()

        // Add a marker in Sydney and move the camera
        val bln = LatLng(52.52316261666667, 13.422810166666666)
        var marker = MarkerOptions()
        marker.title("Marker in Berlin")
        marker.position(bln)
        marker.snippet("")
        mMap.addMarker(marker)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bln))

        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener {
            Toast.makeText(this,"The user touched the map",Toast.LENGTH_LONG).show()
            changeType()
            }

        }



    fun changeType(){

        if (mMap.mapType == GoogleMap.MAP_TYPE_NORMAL){
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
        else{
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    override fun onCameraMove() {
        print("camera moving")
    }

    fun switchView(view:View){
        val intent = Intent(this, Camera::class.java)
        this.flamingoManager?.stopFlamingoService()
        startActivity(intent)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            this.requestCode -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG,"FINE LOCATION PERMISSION GRANTED")
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            456 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG,"READ PHONE STATE PERMISSION GRANTED")
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

}
