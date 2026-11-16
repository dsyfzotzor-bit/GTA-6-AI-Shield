
package com.gtashield.ai

import android.media.projection.MediaProjection

class LiveScreenProtectionSession {

    private var frameCapture: ScreenFrameCapture? = null
    private var frameController: SpoilerLiveFrameController? = null

    private var isRunning = false

    fun start(
        projection: MediaProjection,
        width: Int,
        height: Int,
        densityDpi: Int,
        onReport: (SpoilerAnalysisReport) -> Unit,
        onError: (String) -> Unit,
        onBusy: () -> Unit = {}
    ) {

        if (isRunning) {
            onError("Protection session is already running.")
            return
        }

        val capture = ScreenFrameCapture(projection)
        val controller = SpoilerLiveFrameController()

        frameCapture = capture
        frameController = controller

        capture.start(
            width = width,
            height = height,
            densityDpi = densityDpi,

            onFrame = { bitmap ->

                if (!isRunning) {
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                    return@start
                }

                controller.submitFrame(
                    bitmap = bitmap,
                    onResult = onReport,
                    onError = onError,
                    onBusy = onBusy
                )
            },

            onError = { message ->
                onError(message)
                stop()
            }
        )

        isRunning = capture.isActive()

        if (!isRunning) {
            controller.close()
            frameController = null
            frameCapture = null
        }
    }

    fun stop() {
        isRunning = false

        frameCapture?.stop()
        frameCapture = null

        frameController?.close()
        frameController = null
    }

    fun isActive(): Boolean {
        return isRunning
    }
}
