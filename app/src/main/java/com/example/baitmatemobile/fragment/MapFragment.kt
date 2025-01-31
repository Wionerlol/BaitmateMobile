package com.example.baitmatemobile.fragment

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.baitmatemobile.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class MapFragment : Fragment() {

    private lateinit var googleMap: GoogleMap
    private lateinit var requestQueue: RequestQueue

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        // Move the camera to a default location (Singapore coordinates)
        val singapore = LatLng(1.3521, 103.8198)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 10f))
        fetchFishingHotspots()
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
    }

    private fun fetchFishingHotspots() {
        val url = "http://10.0.2.2:8080/api/locations"

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("MapFragment", "Response received.")
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

                    Log.d("MapFragment", "Adding Marker: $name at ($latitude, $longitude)")

                    val position = LatLng(latitude, longitude)
                    googleMap.addMarker(MarkerOptions().position(position).title(name))
                    fetchWeatherForecast(name, position)
                } catch (e: Exception) {
                    Log.e("MapFragment", "Error parsing location at index $i: ${e.message}")
                }
            }
        } else {
            Log.d("MapFragment", "No locations found in response")
        }

        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }

    private fun fetchWeatherForecast(locationName: String, position: LatLng) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        val url = "https://api-open.data.gov.sg/v2/real-time/api/two-hr-forecast?date=$currentTime"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("MapFragment", "Weather response received.")
                val forecast = parseWeatherForecast(response, position)
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
        googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title(locationName)
                .snippet("2h forecast: $forecast\nValid: $validPeriod")
        )
    }

}