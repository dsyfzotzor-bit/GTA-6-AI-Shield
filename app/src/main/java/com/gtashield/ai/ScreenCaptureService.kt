
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
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_START =
            "com.gtashield.ai.action.START_CAPTURE"

        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"

        private const val CHANNEL_ID = "gta_shield_capture"
        private const val NOTIFICATION_ID = 1001
    }

    private val binder = LocalBinder()

    private var mediaProjection: MediaProjection? = null

    inner class LocalBinder : Binder() {
        fun getService(): ScreenCaptureService = this@ScreenCaptureService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action != ACTION_START) {
            stopSelf()
            return START_NOT_STICKY
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
            return START_NOT_STICKY
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

            if (mediaProjection == null) {
                stopSelf()
                return START_NOT_STICKY
            }

            mediaProjection?.registerCallback(
                object : MediaProjection.Callback() {
                    override fun onStop() {
                        mediaProjection = null
                        stopSelf()
                    }
                },
                Handler(Looper.getMainLooper())
            )

        } catch (error: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        return START_NOT_STICKY
    }

    fun getProjection(): MediaProjection? {
        return mediaProjection
    }

    fun stopCapture() {
        mediaProjection?.stop()
        mediaProjection = null
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
        mediaProjection?.stop()
        mediaProjection = null
        super.onDestroy()
    }
}
