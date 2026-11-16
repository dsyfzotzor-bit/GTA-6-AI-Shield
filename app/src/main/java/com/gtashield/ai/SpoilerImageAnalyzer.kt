
package com.gtashield.ai

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

data class ImageAnalysisResult(
    val labels: List<String>,
    val confidenceScores: List<Float>,
    val containsGamingContent: Boolean
)

class SpoilerImageAnalyzer {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.DEFAULT_OPTIONS
    )

    fun analyze(
        image: InputImage,
        onResult: (ImageAnalysisResult) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {

        labeler.process(image)
            .addOnSuccessListener { labels ->

                val names = labels.map {
                    it.text.lowercase()
                }

                val scores = labels.map {
                    it.confidence
                }

                val gamingTerms = listOf(
                    "video game",
                    "game",
                    "gaming",
                    "vehicle",
                    "car",
                    "computer game",
                    "video game console"
                )

                val gamingDetected = names.any { name ->
                    gamingTerms.any { term ->
                        name.contains(term)
                    }
                }

                onResult(
                    ImageAnalysisResult(
                        labels = names,
                        confidenceScores = scores,
                        containsGamingContent = gamingDetected
                    )
                )
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    fun close() {
        labeler.close()
    }
}
