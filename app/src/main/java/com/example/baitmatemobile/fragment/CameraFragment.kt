package com.example.baitmatemobile.fragment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.baitmatemobile.R
import okhttp3.*
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.IOException

class CameraFragment : Fragment() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_CAMERA_PERMISSION = 100


    private lateinit var imageView: ImageView
    private lateinit var predictButton: Button
    private lateinit var predictResult: TextView
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageBitmap: Bitmap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        imageView = view.findViewById(R.id.imageView)
        predictResult = view.findViewById(R.id.textView)

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageBitmap = result.data?.extras?.get("data") as Bitmap
                imageView.setImageBitmap(imageBitmap)
            }
        }

        view.findViewById<View>(R.id.captureButton).setOnClickListener {
            dispatchTakePictureIntent()
        }

        predictButton = view.findViewById(R.id.predictButton)
        predictButton.setOnClickListener {
            sendImageToServer(imageBitmap)
        }
        return view
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            takePictureLauncher.launch(takePictureIntent)
        }
    }


    private fun sendImageToServer(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "capture.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
            .build()
        val request = Request.Builder()
            .url("http://10.0.2.2:5000/image/predict")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        val jsonArray = JSONArray(responseBody)
                        val result = StringBuilder()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONArray(i)
                            val fishName = item.getString(0)
                            val confidence = item.getDouble(1)
                            result.append("$fishName: $confidence%\n")
                        }
                        println("Image uploaded successfully")
                        predictResult.text = result.toString()
                    } else {
                        println("Image upload failed")
                        println(response.body()?.string())
                    }
                }
            }
        })
    }



//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                dispatchTakePictureIntent()
//            } else {
//                // Permission denied, show a message to the user
//            }
//        }
//    }
}