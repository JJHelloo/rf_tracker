package com.example.rf_tracker

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AuthenticationManager(private val context: Context) {

    fun getExistingToken(): String? {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("JWT_TOKEN", null)
    }

    // Add  SerialNumber as parameters
    fun performLogin(username: String, password: String, SerialNumber: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)
//        json.put("MACAddress", MACAddress) // Add MAC address to JSON
        json.put("SerialNumber", SerialNumber) // Add Serial number to JSON

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://10.0.2.2:3001/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val respString = response.body?.string()

                    // Check if respString is null
                    if (respString != null) {
                        val jsonResponse = JSONObject(respString)
                        if (jsonResponse.has("token")) {
                            val token = jsonResponse.getString("token")
                            val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("JWT_TOKEN", token)
                            editor.apply()

                            onSuccess()
                        } else {
                            onFailure()
                        }
                    } else {
                        // Handle the case where respString is null
                        onFailure()
                    }
                } else {
                    onFailure()
                }
            }

        })
    }

}
