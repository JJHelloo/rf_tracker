package com.example.rf_tracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager


class MainActivity : AppCompatActivity() {
    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 101
    }

    private lateinit var authManager: AuthenticationManager
    private lateinit var devicePolicyManager: MyDevicePolicyManager
    private lateinit var locationManager: LocationManager
    private lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPhoneStatePermission()

        initializeManagers()
        setupUI()
    }

    private fun checkPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }
    }

    private fun initializeManagers() {
        networkManager = NetworkManager()
        locationManager = LocationManager(this, this, networkManager)
        locationManager.initializeLocationManager()
        authManager = AuthenticationManager(this)
        devicePolicyManager = MyDevicePolicyManager(this)
        devicePolicyManager.setupDevicePolicy()
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
        var serialNumber = getSerialNumber()

        authManager.performLogin(username, password, serialNumber, {
            runOnUiThread {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                devicePolicyManager.stopKioskMode()
            }
        }, {
            runOnUiThread {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getSerialNumber(): String {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Build.getSerial()
                } else {
                    Build.SERIAL
                }
            } catch (e: SecurityException) {
                "Unavailable"
            }
        } else {
            Toast.makeText(this, "Permission for reading phone state not granted", Toast.LENGTH_SHORT).show()
            return "Unavailable"
        }
    }
}

