package com.example.rf_tracker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import android.app.IntentService
import android.content.pm.PackageManager
import android.util.Log

class GeofenceManager(private val context: Context) {

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceList: MutableList<Geofence>

    init {
        Log.d("GeofenceManager", "Initializing GeofenceManager")
        setupGeofencingClient()
    }

    private fun setupGeofencingClient() {
        Log.d("GeofenceManager", "Setting up GeofencingClient")
        geofencingClient = LocationServices.getGeofencingClient(context)
    }

    fun registerGeofences() {
        Log.d("GeofenceManager", "Registering Geofences")
        geofenceList = mutableListOf()

        val geofence = Geofence.Builder()
            .setRequestId("WAREHOUSE_GEOFENCE_ID")
            .setCircularRegion(33.97778, -117.60524, 189f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        geofenceList.add(geofence)

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        val intent = Intent(context, GeofenceTransitionsIntentService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)?.run {
                addOnSuccessListener {
                    Log.d("GeofenceManager", "Successfully added geofences")
                }
                addOnFailureListener { e ->
                    Log.e("GeofenceManager", "Failed to add geofences: ${e.message}")
                }
            }
        } else {
            Log.e("GeofenceManager", "Location permission not granted")
        }
    }

    fun unregisterGeofences() {
        Log.d("GeofenceManager", "Unregistering Geofences")
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d("GeofenceManager", "Successfully removed geofences")
            }
            addOnFailureListener { e ->
                Log.e("GeofenceManager", "Failed to remove geofences: ${e.message}")
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
        Log.d("GeofenceService", "Handling geofence transition")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceService", "Error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val devicePolicyManager = MyDevicePolicyManager(this)

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("GeofenceService", "Entering geofence")
                devicePolicyManager?.stopKioskMode() ?: Log.e("GeofenceService", "devicePolicyManager is null")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceService", "Exiting geofence")
                devicePolicyManager?.startKioskMode() ?: Log.e("GeofenceService", "devicePolicyManager is null")
            }
            else -> {
                Log.e("GeofenceService", "Invalid geofence transition type")
            }
        }
    }
}
