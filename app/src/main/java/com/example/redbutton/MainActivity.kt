package com.example.redbutton

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
