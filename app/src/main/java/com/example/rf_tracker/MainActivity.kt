package com.example.rf_tracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    var isInForeground = false

    override fun onResume() {
        super.onResume()
        isInForeground = true
        devicePolicyManager.startKioskMode()
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }
    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 101
        var instance: MainActivity? = null  // Static instance variable
    }

    private lateinit var authManager: AuthenticationManager
    private lateinit var devicePolicyManager: MyDevicePolicyManager
    private lateinit var geofenceManager: GeofenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instance = this  // Store the instance

        // Start the LocationService
        Intent(this, LocationService::class.java).also { intent ->
            startService(intent)
        }

        checkPhoneStatePermission()
        initializeManagers()
        setupUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null  // Clear the instance
    }

    private fun checkPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }
    }

    private fun initializeManagers() {
        authManager = AuthenticationManager(this)
        devicePolicyManager = MyDevicePolicyManager(this)
        devicePolicyManager.setupDevicePolicy()
        geofenceManager = GeofenceManager(this)
    }

    private fun setupUI() {
        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            handleLogin(usernameEditText.text.toString(), passwordEditText.text.toString())
        }
    }

    private fun handleLogin(username: String, password: String) {
        val serialNumber = getSerialNumber()

        // Start kiosk mode here
        // This will ensure kiosk mode is activated when login starts
//        devicePolicyManager.startKioskMode()

        authManager.performLogin(username, password, serialNumber, {
            runOnUiThread {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                Log.d("KioskMode", "Attempting to stop Kiosk Mode after successful login.")
//                devicePolicyManager.stopKioskMode()
                geofenceManager.registerGeofences()
            }
        }, {
            runOnUiThread {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getSerialNumber(): String {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Build.getSerial()
                } else {
                    Build.SERIAL
                }
            } catch (e: SecurityException) {
                "Unavailable"
            }
        } else {
            Toast.makeText(
                this,
                "Permission for reading phone state not granted",
                Toast.LENGTH_SHORT
            ).show()
            "Unavailable"
        }
    }
}
