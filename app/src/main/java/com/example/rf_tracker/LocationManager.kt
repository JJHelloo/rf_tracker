package com.example.rf_tracker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import org.json.JSONObject


class LocationManager(private val context: Context, private val activity: Activity, private val networkManager: NetworkManager) {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var locationManager: LocationManager

    fun initializeLocationManager() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestLocationUpdates()
        }
        // This line starts the LocationService
        startLocationService()
    }

    // This function starts the LocationService
    private fun startLocationService() {
        val serviceIntent = Intent(context, LocationService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            }
        }
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {

            Log.d("LocationDebug", "About to send location data to server")

            val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("JWT_TOKEN", null)  // <-- Fetch the token
//            val username = sharedPreferences.getString("USERNAME", null)  // <-- Fetch the username

            if (token != null) {
                val locationJson = JSONObject()  // <-- Added this line
                locationJson.put("latitude", location.latitude)
                locationJson.put("longitude", location.longitude)
//                locationJson.put("username", username)  // username
                networkManager.sendJSONToServer(locationJson, "http://10.0.2.2:3001/api/location", token)
            } else {
                println("Failed to send location: Token is null")
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Optional to implement
        }

        override fun onProviderEnabled(provider: String) {
            // Optional to implement
        }

        override fun onProviderDisabled(provider: String) {
            // Optional to implement
        }
    }
}