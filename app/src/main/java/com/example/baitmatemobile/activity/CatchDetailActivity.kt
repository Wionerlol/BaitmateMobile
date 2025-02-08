package com.example.baitmatemobile.activity

import android.content.SharedPreferences
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.baitmatemobile.R
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baitmatemobile.model.CatchRecordDTO
import com.example.baitmatemobile.network.ApiService
import com.example.baitmatemobile.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


class CatchDetailActivity : AppCompatActivity() {

    private lateinit var locationSpinner: AutoCompleteTextView
    private lateinit var weightEditText: TextInputEditText
    private lateinit var lengthEditText: TextInputEditText
    private lateinit var remarkEditText: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var locations: List<LocationDTO>
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0

    private val apiService: ApiService by lazy { RetrofitClient.instance }
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catch_detail)

        initViews()
        loadIntentData()
        setupSubmitButton()
        fetchLocations()
    }

    private fun initViews() {
        locationSpinner = findViewById(R.id.locationSpinner)
        weightEditText = findViewById(R.id.weightEditText)
        lengthEditText = findViewById(R.id.lengthEditText)
        remarkEditText = findViewById(R.id.remarkEditText)
        submitButton = findViewById(R.id.submitButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun loadIntentData() {
        userLat = intent.getDoubleExtra("latitude", 0.0)
        userLng = intent.getDoubleExtra("longitude", 0.0)
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            if (validateInput()) {
                submitCatchRecord()
            }
        }
    }

    private fun fetchLocations() = lifecycleScope.launch {
        showLoading(true)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getLocations()
            }

            if (response.isSuccessful) {
                response.body()?.let { locations ->
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
        locationSpinner.setAdapter(adapter)
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (weightEditText.text?.toString().isNullOrBlank()) {
            findViewById<TextInputLayout>(R.id.weightInputLayout).error = "Please enter weight"
            isValid = false
        }

        if (lengthEditText.text?.toString().isNullOrBlank()) {
            findViewById<TextInputLayout>(R.id.lengthInputLayout).error = "Please enter length"
            isValid = false
        }

        return isValid
    }

    private fun submitCatchRecord() = lifecycleScope.launch {
        showLoading(true)
        try {
            val record = createCatchRecord()
            val response = withContext(Dispatchers.IO) {
                apiService.addCatchRecord(record)
            }

            if (response.isSuccessful) {
                showSuccess("Saved！")
                finish()
            } else {
                showError("Submit Failed: ${response.code()}")
            }
        } catch (e: Exception) {
            showError("Submit Failed: ${e.message}")
        } finally {
            showLoading(false)
        }
    }

    private fun createCatchRecord(): CatchRecordDTO {
        val selectedLocationText = locationSpinner.text.toString()
        val selectedLocation = locations.find { it.locationName in selectedLocationText }
            ?: throw IllegalArgumentException("Selected location not found")

        return CatchRecordDTO(
            time = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            image = intent.getByteArrayExtra("image") ?: byteArrayOf(),
            length = lengthEditText.text.toString().toDouble(),
            weight = weightEditText.text.toString().toDouble(),
            latitude = userLat,
            longitude = userLng,
            remark = remarkEditText.text.toString(),
            fishId = intent.getLongExtra("fishId", 0L),
            userId = sharedPreferences.getLong("userId", 0L),
            locationId = selectedLocation.id
        )
    }


    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        submitButton.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    data class LocationDTO(
        @SerializedName("id") val id: Long,
        @SerializedName("locationName") val locationName: String,
        @SerializedName("latitude") val latitude: Double,
        @SerializedName("longitude") val longitude: Double
    )

}