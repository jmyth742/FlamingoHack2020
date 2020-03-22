package com.example.flamingohackathon2020


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.otaliastudios.cameraview.Frame
import flamingo.flamingo_api.FlamingoManager
import flamingo.flamingo_api.utils.ReferenceStationStatus
import kotlinx.android.synthetic.main.activity_camera.*
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import android.view.ViewGroup
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.bounding_box.*

class Camera:
        AppCompatActivity(), SensorEventListener {


    val database = Firebase.database

    val myRef = database.getReference("message")

    lateinit var sensorManager: SensorManager
    lateinit var accelerometer: Sensor
    lateinit var magnetometer: Sensor

    lateinit var usersDBHelper : UsersDBHelper

    var currentDegree = 0.0f
    var lastAccelerometer = FloatArray(3)
    var lastMagnetometer = FloatArray(3)
    var lastAccelerometerSet = false
    var lastMagnetometerSet = false

    var objects:MutableList<RandomObject> = mutableListOf()

    var TAG: String = "Flamingo"
    var bottom = 0
    var left = 0
    var right = 0
    var top = 0
    var label = ""

    val requestCode = 123

    val flamingoListener:GNSSListener = GNSSListener()
    var flamingoManager:FlamingoManager? = null

    //lat: 52.52316261666667 lon: 13.422810166666666

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)

        usersDBHelper = UsersDBHelper(this)

        this.objects = (intent.getSerializableExtra("list") as? Array<RandomObject>)?.toMutableList() ?: mutableListOf()

        //***********************************
        // DYNAMIC BOUNDING BOX
        boundingBox.x = 400f
        boundingBox.y = 500f

        var params = boundingBox.layoutParams;
        params.height = 230
        params.width = 20
        boundingBox.setLayoutParams(params);
        ///*************************************

//        myRef.setValue("hello,world")
//        myRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value = dataSnapshot.getValue<String>()
//                Log.d(TAG, "Value is: $value")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })

        cameraView.setLifecycleOwner(this)
        cameraView.addFrameProcessor {
            extractDataFromFrame(it) { result ->
                tvDetectedObject.text = result
            }
        }

        setUpFlamingo()

    }

    fun setUpFlamingo(){
        //Flamingo


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // We do not have this permission. Let's ask the user
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),456);
        }

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode)




        val flamingoManager = FlamingoManager(this, arrayListOf(flamingoListener))
        val applicationId = getString(R.string.applicationId)
        val password = getString(R.string.password)
        val companyId = getString(R.string.companyId)

        flamingoListener.mContext = this

        flamingoManager.addFlamingoListener(flamingoListener)

        flamingoManager.registerFlamingoService(applicationId, password, companyId,flamingoListener)

        Log.v(TAG,flamingoManager.positioningSession.toString())


        Log.v(TAG, "FLAMINGO REFERENCE STATION STATUS -> " + flamingoManager.referenceStationStatus.toString())
        Log.v(TAG, "FLAMINGO REGISTRATION STATION STATUS -> " + flamingoManager.registrationStatus.toString())

        var counter = 0
        while (flamingoManager.referenceStationStatus != ReferenceStationStatus.AVAILABLE && counter < 10){
            Log.v(TAG,"repeat")
            flamingoManager.registerFlamingoService(applicationId, password, companyId,flamingoListener)
            Thread.sleep(500)
            counter += 1
        }

        //test the distance
        val new_coordinates = CoordinateFinder(52.52316261666667,13.422810166666666).newCoordinate(0.0,0.01)
        Log.v(TAG,"NEW COORDINATES -> " + new_coordinates.toString())
        this.flamingoManager = flamingoManager
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

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
    }



     override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

     override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor === accelerometer) {
            lowPass(event.values, lastAccelerometer)
            lastAccelerometerSet = true
        } else if (event.sensor === magnetometer) {
            lowPass(event.values, lastMagnetometer)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val r = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val degree = (Math.toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360


                currentDegree = -degree
                //Log.d("compass",currentDegree.toString())
            }
        }
    }

    fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.05f

        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    private fun extractDataFromFrame(frame:Frame, callback: (String) -> Unit) {

        val options = FirebaseVisionObjectDetectorOptions.Builder()
                .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                //.enableMultipleObjects()  //Add this if you want to detect multiple objects at once
                .enableClassification()  // Add this if you want to classify the detected objects into categories
                .build()


        val objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)

        val img = getVisionImageFromFrame(frame)

        if(img == null){
            return
        }

        objectDetector.processImage(img!!)
        //objectDetector.processImage(getBitmapImg(frame))
                .addOnSuccessListener {
                    var result = ""
                    it.forEach { item ->

                        result += item.classificationCategory
                        var bounding = item.boundingBox
                        bottom = bounding.bottom
                        left = bounding.left
                        right = bounding.right
                        top = bounding.top
                    }
                    if (result.equals("0"))
                        result = "Unknown"
                    if (result.equals("1"))
                        //usersDBHelper.insertUser(UserModel(userid = result, name = result, age = result))
                        result = "Home Goods"
                    if (result.equals("2"))
                        //usersDBHelper.insertUser(UserModel(userid = result, name = result, age = result))
                        result = "Fashion"
                    if (result.equals("3"))
                        //usersDBHelper.insertUser(UserModel(userid = result, name = result, age = result))
                        result = "Food"
                    if (result.equals("4"))
                        //usersDBHelper.insertUser(UserModel(userid = result, name = result, age = result))
                        result = "Place"
                    if (result.equals("5"))
                        //usersDBHelper.insertUser(UserModel(userid = result, name = result, age = result))
                        result = "Plants"


                    //user id is pos of object
                    //name = result which is typ
                    //age = object pos absolute extrapolated from distance from camera
                    //will need to change this on update of new object which is not unknown

                        //Log.d("DB-Update","updating db - not unknown")

                    callback(result)
                }
                .addOnFailureListener {
                    callback("Unable to detect an object")
                }
    }



    private fun getVisionImageFromFrame(frame : Frame) : FirebaseVisionImage?{

        if (frame.data == null){
            return null
        }

        //ByteArray for the captured frame
        val data = frame.data

        //Metadata that gives more information on the image that is to be converted to FirebaseVisionImage
        val imageMetaData = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(FirebaseVisionImageMetadata.ROTATION_90)
                .setHeight(frame.size.height)
                .setWidth(frame.size.width)
                .build()


        val image = FirebaseVisionImage.fromByteArray(data, imageMetaData)

        return image
    }


    private fun getDistanceToObject(): Float{
        var dist = 0F

        return dist
    }

    fun switchView(view:View){
        val intent = Intent(this, MapsActivity::class.java)
        this.flamingoManager?.stopFlamingoService()
        intent.putExtra("list",objects.toTypedArray())
        startActivity(intent)

    }




}
