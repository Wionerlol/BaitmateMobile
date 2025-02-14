package com.example.baitmatemobile.activity

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.PreviewImagesAdapter
import com.example.baitmatemobile.model.CreatedPostDTO
import com.example.baitmatemobile.model.Image
import com.example.baitmatemobile.model.LocationDTO
import com.example.baitmatemobile.model.Post
import com.example.baitmatemobile.model.User
import com.example.baitmatemobile.network.ApiService
import com.example.baitmatemobile.network.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class UploadPostActivity : AppCompatActivity() {

    private lateinit var btnBack: MaterialButton
    private lateinit var rvPreviewImages: RecyclerView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var spinnerLocation: AutoCompleteTextView
    private lateinit var btnPublish: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var locations: List<LocationDTO>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0

    private val apiService: ApiService by lazy { RetrofitClient.instance }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }


    private lateinit var sharedPrefs: SharedPreferences

    private val editedUris = mutableListOf<Uri>()
    private lateinit var previewAdapter: PreviewImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)


        btnBack = findViewById(R.id.btnBack)
        rvPreviewImages = findViewById(R.id.rvPreviewImages)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        spinnerLocation = findViewById(R.id.spinnerLocation)
        btnPublish = findViewById(R.id.btnPublish)
        progressBar = findViewById(R.id.progressBar)

        requestLocationAndLoadData()
        fetchLocations()

        val uris = intent.getParcelableArrayListExtra<Uri>("selectedImages")
        if (uris != null) {
            editedUris.addAll(uris)
        }
        val imageByteArrays = mutableListOf<ByteArray>()
        uris?.forEach { uri ->
            val byteArray = uriToByteArray(this, uri)
            byteArray?.let { imageByteArrays.add(it) }
        }
        val imageBase64List = imageByteArrays.map { byteArrayToBase64(it) }

        previewAdapter = PreviewImagesAdapter(editedUris)
        rvPreviewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPreviewImages.adapter = previewAdapter

        val locations = listOf("", "West coast park", "East coast park")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            locations
        )
        spinnerLocation.setAdapter(adapter)

        btnBack.setOnClickListener {
            finish()
        }

        btnPublish.setOnClickListener {
            if(validateInput()){
                publishPost(imageBase64List)
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (etTitle.text?.toString().isNullOrBlank()) {
            findViewById<TextInputLayout>(R.id.titleInputLayout).error = "Please enter title"
            isValid = false
        }

        if (etContent.text?.toString().isNullOrBlank()) {
            findViewById<TextInputLayout>(R.id.contentInputLayout).error = "Please enter content"
            isValid = false
        }

        return isValid
    }

    private fun requestLocationAndLoadData() {
        if (checkLocationPermission()) {
            getLastLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    showError("Location permission required")
                    setupDefaultLocation()
                }
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        showLoading(true)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    userLat = it.latitude
                    userLng = it.longitude
                    fetchLocations()
                } ?: run {
                    showError("Unable to get location")
                    showLoading(false)
                }
            }
            .addOnFailureListener { e ->
                showError("Location error: ${e.message}")
                showLoading(false)
            }
    }

    private fun setupDefaultLocation() {
        userLat = 1.3521
        userLng = 103.8198
        fetchLocations()
    }


    private fun fetchLocations() = lifecycleScope.launch {
        showLoading(true)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getLocations()
            }

            if (response.isSuccessful) {
                response.body()?.let { locations: List<LocationDTO> ->
                    val sorted = locations.sortedBy {
                        calculateDistance(userLat, userLng, it.latitude, it.longitude)
                    }
                    setupLocationSpinner(sorted)
                }
            } else {
                showError("Location Fetch Failed: ${response.code()}")
            }
        } catch (e: Exception) {
            showError("Network Error: ${e.message}")
        } finally {
            showLoading(false)
        }
    }

    private fun setupLocationSpinner(locations: List<LocationDTO>) {
        this.locations = locations
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            locations.map {
                "${it.locationName} • ${calculateDistance(userLat, userLng, it.latitude, it.longitude).roundToInt()}m"
            }
        )
        spinnerLocation.setAdapter(adapter)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnPublish.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun publishPost(imageBase64: List<String>) {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val selectedLocationText = spinnerLocation.text.toString()
        val selectedLocation = locations.find { it.locationName in selectedLocationText }

        var userId = sharedPrefs.getLong("userId",0)

        lifecycleScope.launch {
            try {
                val checkImageResults = mutableListOf<String>()
                for(image in imageBase64){
                    val request = ImageCheckRequest(image)
                    val response = RetrofitClient.instance.checkImage(request)
                    if (response.isSuccessful) {
                        val status = response.body()?.status
                        if (status != null) {
                            checkImageResults.add(status)
                        } else {
                            throw Exception("Empty response body")
                        }
                    } else {
                        throw Exception("Image check failed: ${response.code()}")
                    }
                }
                val post = CreatedPostDTO(
                    postTitle = title,
                    postContent = content,
                    userId = userId,
                    location = selectedLocation?.locationName,
                    status = if (checkImageResults.contains("pending")) "pending" else "approved",
                    imageBase64List = imageBase64
                )
                val createdPost = RetrofitClient.instance.createPost(post)
                Toast.makeText(this@UploadPostActivity, "UPLOAD SUCCESSFUL！ID=${createdPost}", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@UploadPostActivity, "FAILED: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class ImageCheckRequest(val image: String)
    data class ImageCheckResponse(
        val status: String
    )

    private fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun byteArrayToBase64(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

}
