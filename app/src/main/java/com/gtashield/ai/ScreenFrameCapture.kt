
package com.gtashield.ai

import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper

class ScreenFrameCapture(
    private val projection: MediaProjection
) {

    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var isRunning = false

    fun start(
        width: Int,
        height: Int,
        densityDpi: Int,
        onFrame: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {

        if (isRunning) {
            onError("Frame capture is already running.")
            return
        }

        if (width <= 0 || height <= 0 || densityDpi <= 0) {
            onError("Invalid screen dimensions.")
            return
        }

        try {
            val reader = ImageReader.newInstance(
                width,
                height,
                android.graphics.PixelFormat.RGBA_8888,
                2
            )

            imageReader = reader

            reader.setOnImageAvailableListener(
                { source ->

                    val image = source.acquireLatestImage()
                        ?: return@setOnImageAvailableListener

                    try {
                        val plane = image.planes[0]
                        val buffer = plane.buffer

                        val pixelStride = plane.pixelStride
                        val rowStride = plane.rowStride

                        val rowPadding =
                            rowStride - pixelStride * width

                        val paddedWidth =
                            width + rowPadding / pixelStride

                        val paddedBitmap = Bitmap.createBitmap(
                            paddedWidth,
                            height,
                            Bitmap.Config.ARGB_8888
                        )

                        paddedBitmap.copyPixelsFromBuffer(buffer)

                        val frame = Bitmap.createBitmap(
                            paddedBitmap,
                            0,
                            0,
                            width,
                            height
                        )

                        paddedBitmap.recycle()

                        onFrame(frame)

                    } catch (error: Exception) {
                        onError(
                            error.message ?: "Frame processing failed."
                        )
                    } finally {
                        image.close()
                    }

                },
                Handler(Looper.getMainLooper())
            )

            virtualDisplay = projection.createVirtualDisplay(
                "GTA6_AI_SHIELD_CAPTURE",
                width,
                height,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                null
            )

            if (virtualDisplay == null) {
                reader.close()
                imageReader = null
                onError("Unable to create virtual display.")
                return
            }

            isRunning = true

        } catch (error: Exception) {
            stop()

            onError(
                error.message ?: "Screen capture initialization failed."
            )
        }
    }

    fun stop() {
        isRunning = false

        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.setOnImageAvailableListener(null, null)
        imageReader?.close()
        imageReader = null
    }

    fun isActive(): Boolean {
        return isRunning
    }
}
