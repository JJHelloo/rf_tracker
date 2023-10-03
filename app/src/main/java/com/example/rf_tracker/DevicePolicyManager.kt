package com.example.rf_tracker

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.app.admin.DevicePolicyManager
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
            if (mainActivity.isInForeground) { // Add this check
                mainActivity.startLockTask()
            } else {
                // Handle the case where the app is not in the foreground
            }
        } ?: showToast("MainActivity instance is null")
    }

    fun stopKioskMode() {
        MainActivity.instance?.let { mainActivity ->
            if (mainActivity.isInForeground) { // Add this check
                mainActivity.stopLockTask()
            } else {
                // Handle the case where the app is not in the foreground
            }
        } ?: showToast("MainActivity instance is null")
    }


    private fun showToast(message: String) {
        uiHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}


