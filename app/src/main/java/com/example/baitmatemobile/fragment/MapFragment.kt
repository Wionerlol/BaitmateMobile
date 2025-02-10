package com.example.baitmatemobile.fragment

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Camera
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.baitmatemobile.R
import com.example.baitmatemobile.model.FishingLocation
import com.example.baitmatemobile.network.RetrofitClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        /*
        googleMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }
            override fun getInfoContents(marker: Marker): View {
                val view = layoutInflater.inflate(R.layout.custom_info_window, null)
                val title = view.findViewById<TextView>(R.id.info_window_title)
                val snippet = view.findViewById<TextView>(R.id.info_window_snippet)

                title.text = marker.title
                snippet.text = marker.snippet

                return view
            }
        })
        */

        googleMap.setOnMarkerClickListener { marker ->
            showBottomSheetDialog(marker)
            true
        }

        // Move the camera to a default location (Singapore coordinates)
        val singapore = LatLng(1.3521, 103.8198)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 10f))
        loadFishingHotspots()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getLong("userId", 0)

        map = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        searchBox = rootView.findViewById(R.id.et_search)
        btnSearch = rootView.findViewById(R.id.btn_search)
        btnMarkFishing = rootView.findViewById(R.id.btn_mark_fishing)
        btnShowHotspots = rootView.findViewById(R.id.btn_show_savedSpots)
        requestQueue = Volley.newRequestQueue(requireContext())
        preloadWeatherForecast()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map.getMapAsync(callback)

        setButtonListeners()
    }

    private fun showBottomSheetDialog(marker: Marker) {
        // Create BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        // Inflate custom layout
        val view = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(view)

        // Access views in the layout
        val titleTextView = view.findViewById<TextView>(R.id.title)
        val snippetTextView = view.findViewById<TextView>(R.id.snippet)
        val saveButton = view.findViewById<Button>(R.id.save_button)

        // Set marker data to the views
        titleTextView.text = marker.title
        snippetTextView.text = marker.snippet

        // Handle "Save" button click
        saveButton.setOnClickListener {
            val locationId = markers.entries.find { it.value == marker }?.key ?: -1L
            if (locationId != -1L) {
                saveLocation(locationId)
            } else {
                Toast.makeText(requireContext(), "Error: Unable to find location ID", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        bottomSheetDialog.show()
    }

    private fun saveLocation(locationId: Long) {
        Log.d("MapFragment", "Attempting to save location for $userId")


        RetrofitClient.instance.saveLocation(userId, locationId).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("MapFragment", "Response code: ${response.code()}")
                Log.d("MapFragment", "Response body: ${response.body()}")
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Locations saved successfully!", Toast.LENGTH_SHORT).show()
                    //updateSaveButtonColor(locationId, true)
                } else {
                    Toast.makeText(requireContext(), "Failed to save location.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MapFragment", "Failed to save location. Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
        if (locations.isNotEmpty()) {
            locations.forEach { location ->
                val position = LatLng(location.latitude, location.longitude)
                val marker = googleMap.addMarker(MarkerOptions().position(position).title(location.locationName))
                if (marker != null) {
                    markers[location.id] = marker
                    marker.tag = layoutInflater.inflate(R.layout.custom_info_window, null)
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
            //marker.showInfoWindow()
            //Log.d("MapFragment", "Marker for $locationName updated with forecast: $forecast and valid period: $validPeriod")
        } else {
            Log.e("MapFragment", "Marker for $locationName not found")
        }
    }

    private fun setButtonListeners() {
        btnSearch.setOnClickListener{
            val query = searchBox.text.toString().trim()
            searchLocation(query)
        }

        btnMarkFishing.setOnClickListener{
            fishingHotspotsData?.let { it1 -> addMarkersToMap(it1) }
        }

        btnShowHotspots.setOnClickListener{
            fetchSavedSpots(userId)
        }
    }

    private fun searchLocation(query: String) {
        if(query.isBlank()) {
            Toast.makeText(requireContext(), "Please enter location to search", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            try {
                val fishingSpots = RetrofitClient.instance.searchFishingSpots(query)
                if (fishingSpots.isNotEmpty()) {
                    val firstSpot = fishingSpots[0]
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(firstSpot.latitude, firstSpot.longitude), 12f)
                    )

                    fetchNearbyFishingSpots(firstSpot.latitude, firstSpot.longitude)
                } else {
                    Toast.makeText(requireContext(), "Could not find nearby spots for $query", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
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

    private fun displayFishingSpotsOnMap(nearbySpots: List<FishingLocation>) {
        val nearbySpotIds = nearbySpots.map { it.id }.toSet()
        for ((spotId, marker) in markers) {
            marker?.isVisible = spotId in nearbySpotIds
        }
    }

    private fun fetchSavedSpots(userId: Long) {
        lifecycleScope.launch {
            try {
                val savedSpots = RetrofitClient.instance.getSavedLocations(userId)

                if (savedSpots != null) {
                    displayFishingSpotsOnMap(savedSpots)
                } else Toast.makeText(requireContext(),"No saved locations", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to fetch: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}