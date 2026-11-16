
package com.gtashield.ai

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SpoilerOverlayManager(context: Context) {

    private val appContext = context.applicationContext

    private val windowManager =
        appContext.getSystemService(
            Context.WINDOW_SERVICE
        ) as WindowManager

    private var overlayView: LinearLayout? = null

    fun hasPermission(): Boolean {
        return Settings.canDrawOverlays(appContext)
    }

    fun show(
        message: String = "GTA 6 CONTENT DETECTED"
    ): Boolean {

        if (!hasPermission()) {
            return false
        }

        if (overlayView != null) {
            return true
        }

        try {
            val container = LinearLayout(appContext).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(40, 40, 40, 40)
                setBackgroundColor(Color.rgb(12, 12, 20))
            }

            val title = TextView(appContext).apply {
                text = "GTA 6 AI SHIELD"
                textSize = 25f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
            }

            val warning = TextView(appContext).apply {
                text = message
                textSize = 19f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(255, 90, 90))
                setPadding(0, 30, 0, 30)
            }

            val description = TextView(appContext).apply {
                text = "This screen was covered to help protect you from unwanted GTA 6 content."
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.LTGRAY)
            }

            val closeButton = Button(appContext).apply {
                text = "CLOSE SHIELD"
                setOnClickListener {
                    hide()
                }
            }

            container.addView(title)
            container.addView(warning)
            container.addView(description)
            container.addView(closeButton)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            windowManager.addView(container, params)
            overlayView = container

            return true

        } catch (error: Exception) {
            overlayView = null
            return false
        }
    }

    fun hide() {
        val view = overlayView ?: return

        try {
            windowManager.removeView(view)
        } catch (_: Exception) {
        }

        overlayView = null
    }

    fun isVisible(): Boolean {
        return overlayView != null
    }
}
