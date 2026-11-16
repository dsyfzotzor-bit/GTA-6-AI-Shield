
package com.gtashield.ai

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics

class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_START =
            "com.gtashield.ai.action.START_CAPTURE"

        const val ACTION_STOP =
            "com.gtashield.ai.action.STOP_CAPTURE"

        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"

        private const val CHANNEL_ID = "gta_shield_capture"
        private const val NOTIFICATION_ID = 1001
    }

    private var mediaProjection: MediaProjection? = null
    private var protectionSession: LiveScreenProtectionSession? = null
    private var responder: SpoilerProtectionResponder? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_STOP -> {
                stopCapture()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                startCapture(intent)
            }

            else -> {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startCapture(intent: Intent) {

        if (mediaProjection != null) {
            return
        }

        val resultCode = intent.getIntExtra(
            EXTRA_RESULT_CODE,
            Activity.RESULT_CANCELED
        )

        val resultData: Intent? =
            if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(
                    EXTRA_RESULT_DATA,
                    Intent::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_RESULT_DATA)
            }

        if (resultCode != Activity.RESULT_OK || resultData == null) {
            stopSelf()
            return
        }

        try {
            createNotificationChannel()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                )
            } else {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification()
                )
            }

            val manager = getSystemService(
                MEDIA_PROJECTION_SERVICE
            ) as MediaProjectionManager

            mediaProjection = manager.getMediaProjection(
                resultCode,
                resultData
            )

            val projection = mediaProjection
                ?: throw IllegalStateException(
                    "MediaProjection could not be created."
                )

            projection.registerCallback(
                object : MediaProjection.Callback() {
                    override fun onStop() {
                        mediaProjection = null
                        stopCapture()
                    }
                },
                android.os.Handler(mainLooper)
            )

            startAnalysis(projection)

        } catch (error: Exception) {
            stopCapture()
        }
    }

    private fun startAnalysis(projection: MediaProjection) {

        val metrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val densityDpi = metrics.densityDpi

        responder = SpoilerProtectionResponder(this)

        val session = LiveScreenProtectionSession()
        protectionSession = session

        session.start(
            projection = projection,
            width = width,
            height = height,
            densityDpi = densityDpi,

            onReport = { report ->
                responder?.handle(report)
            },

            onError = {
                // Capture errors are handled by the session.
            },

            onBusy = {
                // A frame is skipped while analysis is busy.
            }
        )
    }

    private fun stopCapture() {

        protectionSession?.stop()
        protectionSession = null

        responder?.hideProtection()
        responder = null

        mediaProjection?.let {
            mediaProjection = null
            it.stop()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "GTA 6 Shield Screen Protection",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("GTA 6 AI Shield")
            .setContentText("Screen protection session is active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        protectionSession?.stop()
        protectionSession = null

        responder?.hideProtection()
        responder = null

        mediaProjection = null

        super.onDestroy()
    }
}
