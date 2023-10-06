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
            .setCircularRegion(33.976253, -117.605278, 5f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        geofenceList.add(geofence)

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .addGeofences(geofenceList)
            .build()

        val intent = Intent(context, GeofenceTransitionsIntentService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

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

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = applicationContext.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("JWT_TOKEN", null) != null
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            Log.e("GeofenceService", "Received null intent")
            return
        }

        Log.d("GeofenceService", "Handling geofence transition")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceService", "Error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition ?: return

        val devicePolicyManager = MyDevicePolicyManager(applicationContext)

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("GeofenceService", "Entering geofence")
                if (!isUserLoggedIn()) {
                    devicePolicyManager.startKioskMode()
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceService", "Exiting geofence")
                if (isUserLoggedIn()) {
                    devicePolicyManager.stopKioskMode()
                }
            }
            else -> {
                Log.e("GeofenceService", "Invalid geofence transition type: $geofenceTransition")
            }
        }
    }
}


