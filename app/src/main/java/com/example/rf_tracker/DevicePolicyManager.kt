package com.example.rf_tracker

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.app.admin.DevicePolicyManager
import android.util.Log
import android.widget.Toast

class MyDevicePolicyManager(private val context: Context) {
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName = ComponentName(context, MyAdmin::class.java)

    private val uiHandler = Handler(Looper.getMainLooper())

    fun setupDevicePolicy() {
        MainActivity.instance?.let { mainActivity ->
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                val packages = arrayOf(context.packageName)
                devicePolicyManager.setLockTaskPackages(componentName, packages)
                startKioskMode()
            } else {
                showToast("Not owner of the device")
            }
        } ?: showToast("MainActivity instance is null")
    }

    fun startKioskMode() {
        MainActivity.instance?.let { mainActivity ->
            Log.d("KioskMode", "Attempting to start Kiosk Mode. Foreground State: ${mainActivity.isInForeground}")
            if (mainActivity.isInForeground) {
                mainActivity.startLockTask()
                Log.d("KioskMode", "Kiosk Mode Started")
            } else {
                Log.d("KioskMode", "App not in foreground. Kiosk Mode not started.")
            }
        } ?: showToast("MainActivity instance is null")
    }

    fun stopKioskMode() {
        MainActivity.instance?.let { mainActivity ->
            Log.d("KioskMode", "Attempting to stop Kiosk Mode. Foreground State: ${mainActivity.isInForeground}")
            if (mainActivity.isInForeground) {
                mainActivity.stopLockTask()
                Log.d("KioskMode", "Kiosk Mode Stopped")
            } else {
                Log.d("KioskMode", "App not in foreground. Kiosk Mode not stopped.")
            }
        } ?: showToast("MainActivity instance is null")
    }



    private fun showToast(message: String) {
        uiHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}


