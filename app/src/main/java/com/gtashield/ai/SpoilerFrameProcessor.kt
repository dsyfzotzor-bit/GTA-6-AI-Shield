
package com.gtashield.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage

class SpoilerFrameProcessor {

    private val coordinator = SpoilerAnalysisCoordinator()

    fun process(
        bitmap: Bitmap,
        onResult: (SpoilerAnalysisReport) -> Unit,
        onError: (String) -> Unit = {},
        onBusy: () -> Unit = {}
    ) {

        if (bitmap.isRecycled) {
            onError("The supplied image is no longer available.")
            return
        }

        try {
            val inputImage = InputImage.fromBitmap(
                bitmap,
                0
            )

            coordinator.analyze(
                image = inputImage,
                onResult = onResult,
                onBusy = onBusy
            )

        } catch (error: Exception) {
            onError(
                error.message ?: "Unable to process image."
            )
        }
    }

    fun close() {
        coordinator.close()
    }
}
