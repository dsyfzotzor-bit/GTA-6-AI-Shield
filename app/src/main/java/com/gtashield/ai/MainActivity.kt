
package com.gtashield.ai

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private var protectionEnabled = false
    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(12, 12, 20)
        window.navigationBarColor = Color.rgb(12, 12, 20)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.rgb(12, 12, 20))
        }

        val title = TextView(this).apply {
            text = "GTA 6 AI SHIELD"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "SMART SPOILER PROTECTION"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            text = "PROTECTION DISABLED"
            textSize = 17f
            setTextColor(Color.RED)
            gravity = Gravity.CENTER
            setPadding(0, 45, 0, 25)
        }

        toggleButton = Button(this).apply {
            text = "ACTIVATE PROTECTION"
            setOnClickListener {
                protectionEnabled = !protectionEnabled
                updateInterface()
            }
        }

        val aiStatus = TextView(this).apply {
            text = "AI ENGINE: READY"
            textSize = 15f
            setTextColor(Color.rgb(100, 190, 255))
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 15)
        }

        val networkStatus = TextView(this).apply {
            text = "ONLINE / OFFLINE AI READY"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        val counter = TextView(this).apply {
            text = "BLOCKED CONTENT: 0"
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 0)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(statusText)
        root.addView(toggleButton)
        root.addView(aiStatus)
        root.addView(networkStatus)
        root.addView(counter)

        setContentView(root)
    }

    private fun updateInterface() {
        if (protectionEnabled) {
            statusText.text = "PROTECTION ENABLED"
            statusText.setTextColor(Color.GREEN)
            toggleButton.text = "DEACTIVATE PROTECTION"
        } else {
            statusText.text = "PROTECTION DISABLED"
            statusText.setTextColor(Color.RED)
            toggleButton.text = "ACTIVATE PROTECTION"
        }
    }
}
