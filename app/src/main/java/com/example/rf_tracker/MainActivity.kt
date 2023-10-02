package com.example.rf_tracker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 1
        const val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 101
    }

    private lateinit var authManager: AuthenticationManager
    private lateinit var devicePolicyManager: MyDevicePolicyManager
    private lateinit var locationManager: LocationManager
    private lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }

        networkManager = NetworkManager()
        locationManager = LocationManager(this, this, networkManager)
        locationManager.initializeLocationManager()

        authManager = AuthenticationManager(this)
        devicePolicyManager = MyDevicePolicyManager(this)

        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)

        devicePolicyManager.setupDevicePolicy()

        loginButton.setOnClickListener {
            val serialNumber = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_PHONE_STATE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Build.getSerial()
                    } else {
                        "Permission not granted"
                    }
                } else {
                    Build.SERIAL
                }
            } catch (e: SecurityException) {
                "Permission not granted"
            }

            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val macAddress = wifiManager.connectionInfo.macAddress

            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            authManager.performLogin(
                username,
                password,
                macAddress,
                serialNumber,
                {
                    runOnUiThread {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        devicePolicyManager.unlockDevice()
                    }
                },
                {
                    runOnUiThread {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            devicePolicyManager.handleAdminRightsGranted()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to read phone state granted.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this,
                        "Permission to read phone state denied. The app might not work as expected.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}
