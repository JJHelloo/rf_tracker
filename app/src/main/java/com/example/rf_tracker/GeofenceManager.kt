package com.example.rf_tracker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import android.app.IntentService
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofenceStatusCodes
import android.util.Log
import android.content.pm.PackageManager

class GeofenceManager(private val context: Context) {

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceList: MutableList<Geofence>

    init {
        setupGeofencingClient()
    }

    private fun setupGeofencingClient() {
        geofencingClient = LocationServices.getGeofencingClient(context)
    }

    fun registerGeofences() {
        // Initialize the geofence list
        geofenceList = mutableListOf()

        // Add a geofence with hardcoded latitude, longitude, and radius
        val geofence = Geofence.Builder()
            .setRequestId("WAREHOUSE_GEOFENCE_ID")
            .setCircularRegion(
                33.97778,  // Latitude
                -117.60524,  // Longitude
                189f  // Radius in meters (You can adjust this based on the actual size of your warehouse)
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        geofenceList.add(geofence)

        // Create a geofencing request
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        // Create an Intent pointing to the IntentService responsible for handling geofence transitions
        val intent = Intent(context, GeofenceTransitionsIntentService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Register the geofences
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added successfully
                }
                addOnFailureListener {
                    // Failed to add geofences
                }
            }
        }
    }

    fun unregisterGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
            }
            addOnFailureListener {
                // Failed to remove geofences
            }
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceTransitionsIntentService::class.java)
        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

class GeofenceTransitionsIntentService : IntentService("GeofenceTransitionsIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceService", "Error: $errorMessage")
            return
        }

        // Handle the geofence event
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Assuming you can access the Context in this service
        val devicePolicyManager = MyDevicePolicyManager(this)

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("GeofenceService", "Entering geofence")
                devicePolicyManager.stopKioskMode()  // Stop Kiosk Mode when inside the boundary
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceService", "Exiting geofence")
                devicePolicyManager.startKioskMode()  // Start Kiosk Mode when outside the boundary
            }
            else -> {
                Log.e("GeofenceService", "Invalid geofence transition type")
            }
        }
    }
}






