package com.example.baitmatemobile.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.FishResult
import com.example.baitmatemobile.adapter.FishResultAdapter
import com.example.baitmatemobile.model.CatchRecord
import com.example.baitmatemobile.network.RetrofitClient
import okhttp3.*
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CameraFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var predictButton: Button
    private lateinit var saveButton: Button
    private lateinit var resultListView: ListView
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageBitmap: Bitmap
    private lateinit var catchRecord: CatchRecord
    private lateinit var resultAdapter: FishResultAdapter
    private val resultList = mutableListOf<FishResult>()
    private lateinit var selectedItem: FishResult


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        imageView = view.findViewById(R.id.imageView)
        resultListView = view.findViewById(R.id.resultListView)
        resultAdapter = FishResultAdapter(requireContext(), resultList)
        resultListView.adapter = resultAdapter

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

        saveButton = view.findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
             sendCatchRecordToServer(imageBitmap)
        }

        resultListView.setOnItemClickListener { parent, view, position, id ->
            selectedItem = resultList[position]
            // 处理选中的项目内容
            for (i in 0 until parent.childCount) {
                parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT)
            }
            view.setBackgroundColor(Color.LTGRAY)
            println("Selected item: ${selectedItem.fishName}, Confidence: ${selectedItem.confidence}, Image URL: ${selectedItem.imageUrl}")
        }

        return view
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            takePictureLauncher.launch(takePictureIntent)
        } else {
            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        return current.format(formatter)
    }


    private fun sendCatchRecordToServer(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val catchRecord = CatchRecord(
            time = getCurrentTime(),
            image = imageBytes,
            length = 10.0,
            weight = 1.0,
            latitude = 37.7749,
            longitude = -122.4194,
            remark = "Sample remark",
            fishId = selectedItem.fishId,
            userId = 1,
            locationId = 1
        )

        val apiService = OkHttpClient()
        val requestBody = RequestBody.create(MediaType.parse("application/json"), catchRecord.toString())
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/catch-records/add")
            .post(requestBody)
            .build()
        apiService.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("Catch record saved successfully")
                } else {
                    println("Failed to save catch record")
                }
            }

             override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
        })
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
                        resultList.clear()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONArray(i)
                            val fishId = item.getString(0)
                            val fishName = item.getString(1)
                            val confidence = item.getDouble(2)
                            val imageUrl = "http://10.0.2.2:8080/fish/image/$fishId"
                            resultList.add(FishResult(fishId.toLong(), fishName, confidence.toInt(), imageUrl))
                        }
                        resultAdapter.notifyDataSetChanged() // 通知适配器数据变化
                    } else {
                        println("Image upload failed: ${response.code()}")
                    }
                }
            }
        })
    }
}