package com.example.rf_tracker

import org.json.JSONObject

class DatabaseManager {

    fun storeDeviceInfo(json: JSONObject, token: String) {  // Add token parameter here
        // Now using the new method in NetworkManager
        NetworkManager().sendJSONToServer(json, "http://10.0.2.2:3001/devices/api/store_device", token)  // Pass token here
    }
}
