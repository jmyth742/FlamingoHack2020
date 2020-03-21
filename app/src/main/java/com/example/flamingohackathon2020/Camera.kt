package com.example.flamingohackathon2020

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import com.otaliastudios.cameraview.Frame
import kotlinx.android.synthetic.main.activity_camera.*



import android.graphics.*

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroupOverlay
import android.widget.FrameLayout
import com.google.android.gms.vision.CameraSource
import com.otaliastudios.cameraview.CameraView

class Camera: AppCompatActivity() {



    var bottom = 0
    var left = 0
    var right = 0
    var top = 0
    var label = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        cameraView.setLifecycleOwner(this)
        cameraView.addFrameProcessor {
            extractDataFromFrame(it) { result ->
                tvDetectedObject.text = result
            }
        }
    }

    private fun extractDataFromFrame(frame:Frame, callback: (String) -> Unit) {

        val options = FirebaseVisionObjectDetectorOptions.Builder()
                .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                //.enableMultipleObjects()  //Add this if you want to detect multiple objects at once
                .enableClassification()  // Add this if you want to classify the detected objects into categories
                .build()




        val objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)
        objectDetector.processImage(getVisionImageFromFrame(frame))
                .addOnSuccessListener {
                    var result = ""
                    it.forEach { item ->

                        result += item.classificationCategory
                        var bounding = item.boundingBox
                        bottom = bounding.bottom
                        left = bounding.left
                        right = bounding.right
                        top = bounding.top

//                        val drawingView = DrawingView(getApplicationContext(), it)
//                        drawingView.draw(Canvas(frame))
//                        runOnUiThread { imageView.setImageBitmap(bitmap) }

                    }
                    if (result.equals("0"))
                        result = "Unknown"
                    if (result.equals("1"))
                        result = "Home Goods"
                    if (result.equals("2"))
                        result = "Fashion"
                    if (result.equals("3"))
                        result = "Food"
                    if (result.equals("4"))
                        result = "Place"
                    if (result.equals("5"))
                        result = "Plants"
                    callback(result)
                }
                .addOnFailureListener {
                    callback("Unable to detect an object")
                }
    }


    private fun getVisionImageFromFrame(frame : Frame) : FirebaseVisionImage{
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

}


/**
 * DrawingView class:
 *    onDraw() function implements drawing
 *     - boundingBox
 *     - Category
 *     - Confidence ( if Category is not CATEGORY_UNKNOWN )
 */
class DrawingView(context: Context, var visionObjects: List<FirebaseVisionObject>) : View(context) {

    companion object {
        // mapping table for category to strings: drawing strings
        val categoryNames: Map<Int, String> = mapOf(
                FirebaseVisionObject.CATEGORY_UNKNOWN to "Unknown",
                FirebaseVisionObject.CATEGORY_HOME_GOOD to "Home Goods",
                FirebaseVisionObject.CATEGORY_FASHION_GOOD to "Fashion Goods",
                FirebaseVisionObject.CATEGORY_FOOD to "Food",
                FirebaseVisionObject.CATEGORY_PLACE to "Place",
                FirebaseVisionObject.CATEGORY_PLANT to "Plant"
        )
    }

    val MAX_FONT_SIZE = 96F

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

        for (item in visionObjects) {
            // draw bounding box
            pen.color = Color.RED
            pen.strokeWidth = 8F
            pen.style = Paint.Style.STROKE
            val box = item.getBoundingBox()
            canvas.drawRect(box, pen)

            // Draw result category, and confidence
            val tags: MutableList<String> = mutableListOf()
            tags.add("Category: ${categoryNames[item.classificationCategory]}")
            if (item.classificationCategory !=
                    FirebaseVisionObject.CATEGORY_UNKNOWN) {
                tags.add("Confidence: ${item.classificationConfidence!!.times(100).toInt()}%")
            }

            var tagSize = Rect(0, 0, 0, 0)
            var maxLen = 0
            var index: Int = -1

            for ((idx, tag) in tags.withIndex()) {
                if (maxLen < tag.length) {
                    maxLen = tag.length
                    index = idx
                }
            }

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2F

            pen.textSize = MAX_FONT_SIZE
            pen.getTextBounds(tags[index], 0, tags[index].length, tagSize)
            val fontSize: Float = pen.textSize * box.width() / tagSize.width()

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = (box.width() - tagSize.width()) / 2.0F
            if (margin < 0F)margin = 0F

            // draw tags onto bitmap (bmp is in upside down format)
            for ((idx, txt) in tags.withIndex()) {
                canvas.drawText(
                        txt, box.left + margin,
                        box.top + tagSize.height().times(idx + 1.0F), pen
                )
            }
        }
    }
}