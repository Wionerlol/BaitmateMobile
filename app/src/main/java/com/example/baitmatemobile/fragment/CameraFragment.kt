package com.example.baitmatemobile.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.CatchDetailActivity
import com.example.baitmatemobile.adapter.FishResult
import com.example.baitmatemobile.adapter.FishResultAdapter
import com.example.baitmatemobile.network.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.ByteArrayOutputStream
import kotlin.math.round

class CameraFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var predictButton: Button
    private lateinit var saveButton: Button
    private lateinit var resultListView: ListView
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageBitmap: Bitmap
    private lateinit var resultAdapter: FishResultAdapter
    private lateinit var progressBar: ProgressBar
    private val resultList = mutableListOf<FishResult>()
    private lateinit var selectedItem: FishResult
    private val LOCATION_PERMISSION_REQUEST = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        imageView = view.findViewById(R.id.imageView)
        resultListView = view.findViewById(R.id.resultListView)
        progressBar = view.findViewById(R.id.progressBar)
        resultAdapter = FishResultAdapter(requireContext(), resultList)
        resultListView.adapter = resultAdapter

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageBitmap = result.data?.extras?.get("data") as Bitmap
                imageView.setImageBitmap(imageBitmap)
                predictButton.isEnabled = true
            }
        }

        view.findViewById<View>(R.id.captureButton).setOnClickListener {
            dispatchTakePictureIntent()
        }

        predictButton = view.findViewById(R.id.predictButton)
        predictButton.setOnClickListener {
            sendImageToServer(imageBitmap)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        saveButton = view.findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            checkLocationPermission()
        }

        resultListView.setOnItemClickListener { parent, view, position, id ->
            selectedItem = resultList[position]
            for (i in 0 until parent.childCount) {
                parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT)
            }
            view.setBackgroundColor(Color.LTGRAY)
            println("Selected item: ${selectedItem.fishName}, Confidence: ${selectedItem.confidence}, Image URL: ${selectedItem.imageUrl}")
            saveButton.isEnabled = true
        }

        return view
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                navigateToInputDetail(location.latitude, location.longitude)
            } else {
                Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToInputDetail(latitude: Double, longitude: Double) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

        val intent = Intent(requireContext(), CatchDetailActivity::class.java).apply {
            putExtra("image", byteArrayOutputStream.toByteArray())
            putExtra("fishId", selectedItem.fishId)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }

        startActivity(intent)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            takePictureLauncher.launch(takePictureIntent)
        } else {
            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendImageToServer(bitmap: Bitmap) {
        if(resultListView.visibility == View.VISIBLE){
            resultListView.visibility = View.GONE
        }
        saveButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        predictButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    "capture.jpg",
                    RequestBody.create("image/jpeg".toMediaType(), byteArrayOutputStream.toByteArray())
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.uploadImage(imagePart)
                }

                if (response.isSuccessful) {
                    val responseData = response.body() ?: emptyList()
                    val tempList = responseData.map { item ->
                        FishResult(
                            fishId = item[0].toLong(),
                            fishName = item[1],
                            confidence = round(item[2].toDouble()).toInt(),
                            imageUrl = RetrofitClient.retrofit.baseUrl().toString() + "fish/image/${item[0]}"
                        )
                    }
                    resultList.clear()
                    resultList.addAll(tempList)
                    resultAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "Request Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                resultListView.visibility = View.VISIBLE
                predictButton.isEnabled = true
            }
        }
    }
}