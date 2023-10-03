package com.example.rf_tracker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject  // <-- Added this line
import okhttp3.RequestBody.Companion.toRequestBody

class NetworkManager {

    private val client = OkHttpClient()

    fun sendJSONToServer(json: JSONObject, url: String, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val body: RequestBody = json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .post(body)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        println("Failed to send data: ${response.message}")
                    } else {
//                        println("Data sent successfully: ${response.body?.string()}")
                    }
                }
            } catch (e: Exception) {
                println("An error occurred: ${e.message}")
            }
        }
    }
}
