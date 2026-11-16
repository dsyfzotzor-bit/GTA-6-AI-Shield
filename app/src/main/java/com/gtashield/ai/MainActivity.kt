
package com.gtashield.ai

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    companion object {
        private const val REQUEST_SCREEN_CAPTURE = 2001
    }

    private var protectionEnabled = false
    private var pendingOverlayRequest = false

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
                if (protectionEnabled) {
                    stopProtection()
                } else {
                    requestRequiredPermissions()
                }
            }
        }

        val aiStatus = TextView(this).apply {
            text = "AI ANALYSIS COMPONENTS CONFIGURED"
            textSize = 14f
            setTextColor(Color.rgb(100, 190, 255))
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 15)
        }

        val networkStatus = TextView(this).apply {
            text = "LOCAL ANALYSIS MODE"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        val counter = TextView(this).apply {
            text = "BLOCKED CONTENT: NOT CONNECTED"
            textSize = 14f
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

    private fun requestRequiredPermissions() {

        if (!Settings.canDrawOverlays(this)) {

            pendingOverlayRequest = true

            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            startActivity(intent)
            return
        }

        requestScreenCapturePermission()
    }

    override fun onResume() {
        super.onResume()

        if (pendingOverlayRequest) {
            pendingOverlayRequest = false

            if (Settings.canDrawOverlays(this)) {
                requestScreenCapturePermission()
            } else {
                statusText.text = "OVERLAY PERMISSION REQUIRED"
            }
        }
    }

    private fun requestScreenCapturePermission() {

        val manager = getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        startActivityForResult(
            manager.createScreenCaptureIntent(),
            REQUEST_SCREEN_CAPTURE
        )
    }

    @Deprecated("Required for this Activity result implementation")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SCREEN_CAPTURE) {

            if (resultCode == Activity.RESULT_OK && data != null) {
                startProtectionService(resultCode, data)
            } else {
                statusText.text = "SCREEN CAPTURE PERMISSION DENIED"
                statusText.setTextColor(Color.RED)
            }
        }
    }

    private fun startProtectionService(
        resultCode: Int,
        data: Intent
    ) {

        val serviceIntent = Intent(
            this,
            ScreenCaptureService::class.java
        ).apply {
            action = ScreenCaptureService.ACTION_START

            putExtra(
                ScreenCaptureService.EXTRA_RESULT_CODE,
                resultCode
            )

            putExtra(
                ScreenCaptureService.EXTRA_RESULT_DATA,
                data
            )
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            protectionEnabled = true
            updateInterface("PROTECTION START REQUESTED")

        } catch (error: Exception) {
            protectionEnabled = false
            updateInterface("UNABLE TO START PROTECTION")
        }
    }

    private fun stopProtection() {

        stopService(
            Intent(this, ScreenCaptureService::class.java)
        )

        protectionEnabled = false
        updateInterface("PROTECTION DISABLED")
    }

    private fun updateInterface(message: String) {

        statusText.text = message

        if (protectionEnabled) {
            statusText.setTextColor(
                Color.rgb(255, 190, 70)
            )

            toggleButton.text = "DEACTIVATE PROTECTION"

        } else {
            statusText.setTextColor(Color.RED)
            toggleButton.text = "ACTIVATE PROTECTION"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
