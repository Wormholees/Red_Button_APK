package com.example.redbutton

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // На Android 13+ нужно разрешение на уведомления, иначе не видно уведомление сервиса
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        startServerService()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F4F4F4"))
        }

        layout.addView(makeButton("Окно 1 (8300)") { Tab1Activity::class.java })
        layout.addView(makeButton("Окно 2 (8400)") { Tab2Activity::class.java })
        layout.addView(makeButton("Окно 3 (8500)") { Tab3Activity::class.java })

        setContentView(layout)
    }

    private fun startServerService() {
        val intent = Intent(this, ServerService::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun makeButton(label: String, target: () -> Class<*>): Button {
        return Button(this).apply {
            text = label
            setBackgroundColor(Color.RED)
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(96, 40, 96, 40)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, target()))
            }
        }
    }
}
