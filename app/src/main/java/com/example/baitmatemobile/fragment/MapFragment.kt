package com.example.baitmatemobile.fragment

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.baitmatemobile.R
import com.example.baitmatemobile.model.FishingLocation
import com.example.baitmatemobile.network.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
import com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt


class MapFragment : Fragment() {

    private lateinit var requestQueue: RequestQueue
    private val markers = HashMap<Long, Marker>()
    private var userId: Long = 0

    private var fishingHotspotsData: List<FishingLocation>? = null
    private var weatherForecastResponse: JSONObject? = null

    private lateinit var map: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var searchBox: AutoCompleteTextView
    private lateinit var btnSearch: Button
    private lateinit var btnMarkFishing: Button
    private lateinit var btnShowHotspots: Button
    private lateinit var searchAdapter: ArrayAdapter<String>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnToggleNightMode: FloatingActionButton

    private var userLocationMarker: Marker? = null

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        setMapStyle(isNightMode)
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
        googleMap.setOnMarkerClickListener { marker ->
            showBottomSheetDialog(marker)
            true
        }
        loadFishingHotspots()
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    if (userLocationMarker == null) {
                        userLocationMarker = googleMap.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title("My Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                    } else {
                        userLocationMarker!!.position = userLatLng
                    }

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)

        setMapStyle(isNightMode)

        userId = sharedPreferences.getLong("userId", 0)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyCrE4w3aiRcrG6-DcuaaN-dMGcrZBeid80")
        }
        placesClient = Places.createClient(requireContext())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        map = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        searchBox = rootView.findViewById(R.id.et_search)
        btnSearch = rootView.findViewById(R.id.btn_search)
        btnMarkFishing = rootView.findViewById(R.id.btn_mark_fishing)
        btnShowHotspots = rootView.findViewById(R.id.btn_show_savedSpots)
        btnToggleNightMode = rootView.findViewById(R.id.btn_toggle_night_mode)
        requestQueue = Volley.newRequestQueue(requireContext())

        btnToggleNightMode.setImageResource(
            if (isNightMode) R.drawable.ic_sun_mode else R.drawable.ic_night_mode
        )

        preloadWeatherForecast()

        setupAutoCompleteSearch()

        setButtonListeners()

        return rootView
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map.getMapAsync(callback)

        setButtonListeners()
    }

    private fun setButtonListeners() {
        btnSearch.setOnClickListener {
            val query = searchBox.text.toString().trim()
            searchLocation(query)
        }
        btnMarkFishing.setOnClickListener {
            if (fishingHotspotsData == null) {
                Log.d("MapFragment", "Fetching fishing hotspots after button click.")
                fetchFishingHotspots()
            } else {
                Log.d("MapFragment", "Fishing hotspots already loaded, showing markers.")
                addMarkersToMap(fishingHotspotsData!!)
            }
        }
        btnShowHotspots.setOnClickListener {
            fetchSavedSpots(userId)
        }

        btnToggleNightMode.setOnClickListener {
            val isNightMode = sharedPreferences.getBoolean("isNightMode", false)
            val newMode = !isNightMode
            sharedPreferences.edit().putBoolean("isNightMode", newMode).apply()

            googleMap.setMapStyle(null)
            setMapStyle(newMode)

            val iconRes = if (newMode) R.drawable.ic_sun_mode else R.drawable.ic_night_mode
            btnToggleNightMode.setImageResource(iconRes)

            Log.d("MapFragment", "夜间模式已切换: $newMode")
        }
    }

    private fun showBottomSheetDialog(marker: Marker) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(view)

        val titleTextView = view.findViewById<TextView>(R.id.title)
        val snippetTextView = view.findViewById<TextView>(R.id.snippet)
        val saveButton = view.findViewById<Button>(R.id.save_button)

        titleTextView.text = marker.title
        snippetTextView.text = marker.snippet

        val locationId = markers.entries.find { it.value == marker }?.key ?: -1L
        if (locationId != -1L) {
            Log.d("MapFragment", "Checking if user $userId has saved location $locationId")
            val isLocationSaved = checkSavedLocations(locationId) { isSaved ->
                if (isSaved) {
                    saveButton.text = "Saved"
                    saveButton.setBackgroundColor(Color.parseColor("#9E9E9E"))
                } else {
                    saveButton.text = "Save"
                    saveButton.setBackgroundColor(Color.parseColor("#4CAF50"))
                }
            }
        }
        saveButton.setOnClickListener {
            if (saveButton.text == "Saved") {
                removeLocation(locationId)
                bottomSheetDialog.dismiss()
            } else {
                saveLocation(locationId)
                bottomSheetDialog.dismiss()
            }
        }
        bottomSheetDialog.show()
    }

    private fun checkSavedLocations(locationId: Long, callback: (Boolean) -> Unit) {
        lifecycleScope.launch {
            val savedLocations: List<FishingLocation>? = RetrofitClient.instance.getSavedLocations(userId)
            val isSaved = savedLocations?.any { it.id == locationId } ?: false
            callback(isSaved)
        }
    }

    private fun saveLocation(locationId: Long) {
        RetrofitClient.instance.saveLocation(userId, locationId).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Location saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to save location. ${response.errorBody()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MapFragment", "Failed to save location. Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeLocation(locationId: Long) {
        RetrofitClient.instance.removeLocation(userId, locationId).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Location removed from saved spots!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to remove location. ${response.errorBody()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MapFragment", "Failed to remove location. Error: ${t.message}")
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadFishingHotspots() {
        if (fishingHotspotsData != null) {
            addMarkersToMap(fishingHotspotsData!!)
        } else {
            fetchFishingHotspots()
        }
    }

    private fun preloadWeatherForecast() {
        val url = "https://api-open.data.gov.sg/v2/real-time/api/two-hr-forecast"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("MapFragment", "Weather forecast preloaded")
                weatherForecastResponse = response
            },
            { error ->
                Log.e("MapFragment", "Error preloading weather forecast: ${error.message}")
            }
        )
        requestQueue.add(request)
    }

    private fun fetchFishingHotspots() {
        // If hotspots were not preloaded, fetch them.
        if (fishingHotspotsData != null) {
            addMarkersToMap(fishingHotspotsData!!)
            return
        }

        RetrofitClient.instance.getFishingLocations().enqueue(object : Callback<List<FishingLocation>> {
            override fun onResponse(call: Call<List<FishingLocation>>, response: Response<List<FishingLocation>>) {
                if (response.isSuccessful) {
                    Log.d("MapFragment", "Response received.")
                    val locations = response.body()

                    if (locations != null) {
                        fishingHotspotsData = locations
                        addMarkersToMap(fishingHotspotsData!!)
                    } else {
                        Log.e("MapFragment", "Received empty response body.")
                    }
                } else {
                    Log.e("MapFragment", "Request failed with status: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<FishingLocation>>, t: Throwable) {
                Log.e("MapFragment", "Request error: ${t.message}")
            }
        })
    }

    private fun addMarkersToMap(locations: List<FishingLocation>) {
        googleMap.clear()
        markers.clear()
        if (locations.isNotEmpty()) {
            locations.forEach { location ->
                val position = LatLng(location.latitude, location.longitude)
                val marker = googleMap.addMarker(
                    MarkerOptions().position(position).title(location.locationName)
                )
                if (marker != null) {
                    markers[location.id] = marker
                }
                fetchWeatherForecast(location.locationName, position)
            }
        }
    }


    private fun fetchWeatherForecast(locationName: String, position: LatLng) {
        val forecast = if (weatherForecastResponse != null) {
            parseWeatherForecast(weatherForecastResponse!!, position)
        } else {
            "No forecast available"
        }
        val validPeriod = parseValidPeriod(weatherForecastResponse!!)
        updateMarkerWithWeather(locationName, forecast, validPeriod)
    }

    private fun parseWeatherForecast(response: JSONObject, position: LatLng): String {
        val areaMetaData = response.getJSONObject("data").getJSONArray("area_metadata")
        val forecasts = response.getJSONObject("data").getJSONArray("items").getJSONObject(0).getJSONArray("forecasts")
        var nearestForecast = "No forecast available"
        var minDistance = Double.MAX_VALUE

        for (i in 0 until areaMetaData.length()) {
            val area = areaMetaData.getJSONObject(i)
            val labelLocation = area.getJSONObject("label_location")
            val areaLatLng = LatLng(labelLocation.getDouble("latitude"), labelLocation.getDouble("longitude"))
            val distance = calculateDistance(position, areaLatLng)

            if (distance < minDistance) {
                minDistance = distance
                nearestForecast = forecasts.getJSONObject(i).getString("forecast")
            }
        }
        return nearestForecast

    }

    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val latDiff = from.latitude - to.latitude
        val lonDiff = from.longitude - to.longitude
        return sqrt(latDiff.pow(2) + lonDiff.pow(2))
    }

    private fun parseValidPeriod(response: JSONObject): String {
        val validPeriod = response.getJSONObject("data").getJSONArray("items").getJSONObject(0).getJSONObject("valid_period")
        return validPeriod.getString("text")
    }

    private fun updateMarkerWithWeather(locationName: String, forecast: String, validPeriod: String) {
        val marker = markers.values.find { it.title == locationName }
        if (marker!=null) {
            marker.snippet = "2h forecast: $forecast\n" +
                    "Valid: $validPeriod"
        } else {
            Log.e("MapFragment", "Marker for $locationName not found")
        }
    }

    private fun searchLocation(query: String) {
        if (query.isBlank()) {
            Toast.makeText(requireContext(), "Please enter location to search", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val fishingSpots = RetrofitClient.instance.searchFishingSpots(query)
                if (fishingSpots.isNotEmpty()) {
                    val firstSpot = fishingSpots[0]
                    val latLng = LatLng(firstSpot.latitude, firstSpot.longitude)

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    fetchNearbyFishingSpots(firstSpot.latitude, firstSpot.longitude)

                    return@launch
                }
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocationName(query, 1)

                if (addresses.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val location = addresses[0]
                val searchedLatLng = LatLng(location.latitude, location.longitude)

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 12f))

                googleMap.addMarker(MarkerOptions().position(searchedLatLng).title(query))

                fetchNearbyFishingSpots(location.latitude, location.longitude)

            } catch (e: Exception) {
                Log.e("MapFragment", "Error searching location: ${e.message}")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchNearbyFishingSpots(latitude: Double, longtitude: Double) {
        lifecycleScope.launch {
            try {
                val nearbySpots = RetrofitClient.instance.getNearbyFishingSpots(latitude, longtitude, 2.0)

                displayFishingSpotsOnMap(nearbySpots)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to fetch: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayFishingSpotsOnMap(nearbySpots: List<FishingLocation>, isSavedSpots: Boolean = false) {
        val nearbySpotIds = nearbySpots.map { it.id }.toSet()
        for ((spotId, marker) in markers) {
            marker?.isVisible = spotId in nearbySpotIds
            marker.setIcon(defaultMarker(HUE_GREEN))
        }
    }

    private fun fetchSavedSpots(userId: Long) {
        lifecycleScope.launch {
            try {
                val savedSpots = RetrofitClient.instance.getSavedLocations(userId)

                if (savedSpots != null) {
                    displayFishingSpotsOnMap(savedSpots, isSavedSpots = true)
                } else {
                    Toast.makeText(requireContext(), "No saved locations", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to fetch: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAutoCompleteSearch() {
        searchBox.setAdapter(null)
        searchBox.threshold = 1

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 2) {
                    fetchAutoCompleteSuggestions(s.toString())
                }
            }
        })

        searchBox.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            searchBox.setText(selectedItem)
            searchLocation(selectedItem)
        }
    }

    private fun fetchAutoCompleteSuggestions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions.map { it.getFullText(null).toString() }
                if (predictions.isNotEmpty()) {
                    searchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, predictions)
                    searchBox.setAdapter(searchAdapter)
                    searchAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapFragment", "Autocomplete fetch failed: ${exception.message}")
            }
    }
    private fun setMapStyle(isNightMode: Boolean) {
        try {
            val styleRes = if (isNightMode) R.raw.night_mode_map else R.raw.day_mode_map
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), styleRes))

            if (!success) {
                Log.e("MapFragment", "地图样式解析失败")
            }
        } catch (e: Exception) {
            Log.e("MapFragment", "应用地图样式时出错: ${e.message}")
        }
    }
}