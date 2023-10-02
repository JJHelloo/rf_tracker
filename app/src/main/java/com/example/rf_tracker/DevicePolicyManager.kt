package com.example.rf_tracker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.admin.DevicePolicyManager
import android.widget.Toast

class MyDevicePolicyManager(private val context: Context) {
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName = ComponentName(context, MyAdmin::class.java)

    // Function to set up device policies
    fun setupDevicePolicy() {
        // Check if this app is the device owner
        if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
            val packages = arrayOf(context.packageName)
            devicePolicyManager.setLockTaskPackages(componentName, packages)
            (context as MainActivity).startLockTask()
        } else {
            Toast.makeText(context, "Not owner of the device", Toast.LENGTH_SHORT).show()
        }

        // Check if app is authorized to lock the device
        if (devicePolicyManager.isLockTaskPermitted(context.packageName)) {
            (context as MainActivity).startLockTask()
        } else {
            Toast.makeText(context, "App is not authorized to lock the device.", Toast.LENGTH_SHORT).show()
        }

        // Request device admin rights
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission")
        (context as MainActivity).startActivityForResult(intent, MainActivity.REQUEST_CODE)
    }

    // Function to unlock the device from LockTask mode
    fun unlockDevice() {
        (context as MainActivity).stopLockTask()
    }

    // Function to handle when admin rights are granted
    fun handleAdminRightsGranted() {
        // The user allowed the admin rights
    }

    // Function to lock the device screen
    fun lockDevice() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        } else {
            Toast.makeText(context, "Not Device Admin", Toast.LENGTH_SHORT).show()
        }
    }
}
