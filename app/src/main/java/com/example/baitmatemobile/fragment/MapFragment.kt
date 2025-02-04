package com.example.baitmatemobile.fragment

import android.content.Context
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.baitmatemobile.R
import com.example.baitmatemobile.network.RetrofitClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.pow
import kotlin.math.sqrt

class MapFragment : Fragment() {

    private lateinit var googleMap: GoogleMap
    private lateinit var requestQueue: RequestQueue
    private val markers = mutableMapOf<String, Marker?>()

    private var fishingHotspotsData: JSONArray? = null
    private var weatherForecastResponse: JSONObject? = null

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

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

        // Move the camera to a default location (Singapore coordinates)
        val singapore = LatLng(1.3521, 103.8198)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 10f))
        if (fishingHotspotsData != null) {
            addMarkersToMap(fishingHotspotsData!!)
        } else {
            fetchFishingHotspots()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        requestQueue = Volley.newRequestQueue(requireContext())
        preloadFishingHotspots()
        preloadWeatherForecast()
    }

    private fun preloadFishingHotspots() {
        val url = "http://10.0.2.2:8080/api/locations"
        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("MapFragment", "Hotspots preloaded: ${response.length()} locations")
                fishingHotspotsData = response
            },
            { error ->
                Log.e("MapFragment", "Error preloading hotspots: ${error.message}")
            }
        )
        requestQueue.add(request)
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

        val url = "http://10.0.2.2:8080/api/locations"
        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("MapFragment", "Response received.")
                fishingHotspotsData = response
                addMarkersToMap(response)
            },
            { error ->
                Log.e("MapFragment", "Request error: ${error.message}")
                error.printStackTrace()
            }
        )
        requestQueue.add(request)
    }

    private fun addMarkersToMap(response: JSONArray) {
        Log.d("MapFragment", "Parsing response, total locations: ${response.length()}")
        if (response.length()>0) {
            for (i in 0 until response.length()) {
                try{
                    val location = response.getJSONObject(i)
                    val name = location.getString("locationName")
                    val latitude = location.getDouble("latitude")
                    val longitude = location.getDouble("longitude")

                    //Log.d("MapFragment", "Adding Marker: $name at ($latitude, $longitude)")

                    val position = LatLng(latitude, longitude)
                    val marker = googleMap.addMarker(MarkerOptions().position(position).title(name))
                    markers[name] = marker
                    fetchWeatherForecast(name, position)
                } catch (e: Exception) {
                    Log.e("MapFragment", "Error parsing location at index $i: ${e.message}")
                }
            }
        } else {
            Log.d("MapFragment", "No locations found in response")
        }

    }

    private fun fetchWeatherForecast(locationName: String, position: LatLng) {
        // Use the preloaded weather forecast if available.
        if (weatherForecastResponse != null) {
            val forecast = parseWeatherForecast(weatherForecastResponse!!, position)
            val validPeriod = parseValidPeriod(weatherForecastResponse!!)
            updateMarkerWithWeather(position, locationName, forecast, validPeriod)
        } else {
            // If not preloaded, make a request.
            val url = "https://api-open.data.gov.sg/v2/real-time/api/two-hr-forecast"
            val request = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    val forecast = parseWeatherForecast(response, position)
                    //Log.d("MapFragment", "Forecast for $locationName updated to $forecast")
                    val validPeriod = parseValidPeriod(response)
                    updateMarkerWithWeather(position, locationName, forecast, validPeriod)
                },
                { error ->
                    Log.e("MapFragment", "Weather request error: ${error.message}")
                    error.printStackTrace()
                }
            )
            requestQueue.add(request)
        }
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

    private fun updateMarkerWithWeather(position: LatLng, locationName: String, forecast: String, validPeriod: String) {
        val marker = markers[locationName]
        if (marker!=null) {
            marker.snippet = "2h forecast: $forecast\n" +
                    "Valid: $validPeriod"
            //marker.showInfoWindow()
            //Log.d("MapFragment", "Marker for $locationName updated with forecast: $forecast and valid period: $validPeriod")
        } else {
            Log.e("MapFragment", "Marker for $locationName not found")
        }
    }

}