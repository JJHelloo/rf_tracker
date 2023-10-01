package com.example.rf_tracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import org.json.JSONObject
import android.widget.Toast
import android.content.Context
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.app.Activity
import android.content.ComponentName

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val existingToken = sharedPreferences.getString("JWT_TOKEN", null)

        if (existingToken != null) {
            // Token exists, you might want to validate it or direct the user to the main activity
            Toast.makeText(this, "Token already exists", Toast.LENGTH_SHORT).show()
        } else {
            // No existing token, keep the user at the login screen
            Toast.makeText(this, "No existing token found", Toast.LENGTH_SHORT).show()
        }
        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)

        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, MyAdmin::class.java)

        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            val packages = arrayOf(packageName)
            devicePolicyManager.setLockTaskPackages(componentName, packages)
        } else {
            Toast.makeText(this, "Not owner of the device", Toast.LENGTH_SHORT).show()
        }

        if (devicePolicyManager.isLockTaskPermitted(this.packageName)) {
            startLockTask()
        } else {
            Toast.makeText(this, "App is not authorized to lock the device.", Toast.LENGTH_SHORT).show()

            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission")
            startActivityForResult(intent, REQUEST_CODE)
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val json = JSONObject()
            json.put("username", username)
            json.put("password", password)

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val client = OkHttpClient()

            val request = Request.Builder()
                .url("http://10.0.2.2:3001/login")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("Request Error", "Network error", e)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        val respString = response.body?.string()
                        val jsonResponse = JSONObject(respString)

                        // Log the entire JSON response
                        Log.d("JSON Response", jsonResponse.toString())

                        // Check if token exists in JSON response
                        if (jsonResponse.has("token")) {
                            val token = jsonResponse.getString("token")

                            val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("JWT_TOKEN", token)
                            editor.apply()

                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                stopLockTask()  // Unlock the device upon successful login
                            }
                        } else {
                            Log.e("Response Error", "Token not found in JSON response")
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Token not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The user allowed the admin rights
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
