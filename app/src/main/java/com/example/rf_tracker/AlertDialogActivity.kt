package com.example.rf_tracker

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AlertDialogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder = AlertDialog.Builder(this)
        builder.setMessage("You've exited the geofence!")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
        val alert = builder.create()
        alert.show()
    }
}
