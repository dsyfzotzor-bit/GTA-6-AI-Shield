
package com.gtashield.ai

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper

class ScreenCaptureManager(
    private val context: Context
) {

    private val projectionManager =
        context.getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

    private var mediaProjection: MediaProjection? = null

    fun createCaptureIntent(): Intent {
        return projectionManager.createScreenCaptureIntent()
    }

    fun start(
        resultCode: Int,
        resultData: Intent,
        onReady: () -> Unit,
        onError: (String) -> Unit
    ) {

        try {
            if (mediaProjection != null) {
                onError("Screen capture is already active.")
                return
            }

            mediaProjection =
                projectionManager.getMediaProjection(
                    resultCode,
                    resultData
                )

            if (mediaProjection == null) {
                onError("Unable to start screen capture.")
                return
            }

            mediaProjection?.registerCallback(
                object : MediaProjection.Callback() {

                    override fun onStop() {
                        mediaProjection = null
                    }

                },
                Handler(Looper.getMainLooper())
            )

            onReady()

        } catch (error: Exception) {
            mediaProjection = null

            onError(
                error.message ?: "Screen capture failed."
            )
        }
    }

    fun getProjection(): MediaProjection? {
        return mediaProjection
    }

    fun stop() {
        mediaProjection?.stop()
        mediaProjection = null
    }

    fun isActive(): Boolean {
        return mediaProjection != null
    }
}
