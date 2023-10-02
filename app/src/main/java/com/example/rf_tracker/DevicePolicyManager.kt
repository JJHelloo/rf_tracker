package com.example.rf_tracker


import android.content.ComponentName
import android.content.Context
import android.app.admin.DevicePolicyManager
import android.widget.Toast

class MyDevicePolicyManager(private val context: Context) {
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName = ComponentName(context, MyAdmin::class.java)

    fun setupDevicePolicy() {
        if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
            val packages = arrayOf(context.packageName)
            devicePolicyManager.setLockTaskPackages(componentName, packages)
            startKioskMode()
        } else {
            Toast.makeText(context, "Not owner of the device", Toast.LENGTH_SHORT).show()
        }
    }

    fun startKioskMode() {
        (context as MainActivity).startLockTask()
    }

    fun stopKioskMode() {
        (context as MainActivity).stopLockTask()
    }
}
