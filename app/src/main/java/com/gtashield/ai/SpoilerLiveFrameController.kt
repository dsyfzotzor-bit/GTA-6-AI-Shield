
package com.gtashield.ai

import android.graphics.Bitmap
import java.util.concurrent.atomic.AtomicBoolean

class SpoilerLiveFrameController {

    private val frameProcessor = SpoilerFrameProcessor()

    private val isProcessing = AtomicBoolean(false)

    fun submitFrame(
        bitmap: Bitmap,
        onResult: (SpoilerAnalysisReport) -> Unit,
        onError: (String) -> Unit = {},
        onBusy: () -> Unit = {}
    ) {

        if (bitmap.isRecycled) {
            onError("Frame is no longer available.")
            return
        }

        if (!isProcessing.compareAndSet(false, true)) {
            bitmap.recycle()
            onBusy()
            return
        }

        frameProcessor.process(
            bitmap = bitmap,

            onResult = { report ->
                try {
                    onResult(report)
                } finally {
                    releaseFrame(bitmap)
                    isProcessing.set(false)
                }
            },

            onError = { message ->
                releaseFrame(bitmap)
                isProcessing.set(false)
                onError(message)
            },

            onBusy = {
                releaseFrame(bitmap)
                isProcessing.set(false)
                onBusy()
            }
        )
    }

    private fun releaseFrame(bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }

    fun isProcessing(): Boolean {
        return isProcessing.get()
    }

    fun close() {
        frameProcessor.close()
    }
}
