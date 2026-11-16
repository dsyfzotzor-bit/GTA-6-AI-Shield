
package com.gtashield.ai

import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

data class SpoilerAnalysisReport(
    val decision: SpoilerDecision,
    val extractedText: String,
    val classification: GtaClassification?,
    val imageAnalysis: ImageAnalysisResult?,
    val warnings: List<String>
)

class SpoilerAnalysisCoordinator {

    private val textAnalyzer = SpoilerTextAnalyzer()
    private val imageAnalyzer = SpoilerImageAnalyzer()
    private val classifier = GtaContentClassifier()
    private val decisionEngine = SpoilerDecisionEngine()

    private val isProcessing = AtomicBoolean(false)

    fun analyze(
        image: InputImage,
        onResult: (SpoilerAnalysisReport) -> Unit,
        onBusy: () -> Unit = {}
    ) {

        if (!isProcessing.compareAndSet(false, true)) {
            onBusy()
            return
        }

        val warnings = mutableListOf<String>()

        textAnalyzer.analyze(
            image = image,
            onResult = { textResult ->

                val extractedText = textResult.extractedText
                val classification = classifier.classify(extractedText)

                analyzeImage(
                    image = image,
                    extractedText = extractedText,
                    classification = classification,
                    textResult = textResult,
                    warnings = warnings,
                    onResult = onResult
                )
            },
            onError = { error ->

                warnings.add(
                    "Text analysis failed: ${error.message ?: "Unknown error"}"
                )

                analyzeImage(
                    image = image,
                    extractedText = "",
                    classification = null,
                    textResult = null,
                    warnings = warnings,
                    onResult = onResult
                )
            }
        )
    }

    private fun analyzeImage(
        image: InputImage,
        extractedText: String,
        classification: GtaClassification?,
        textResult: SpoilerResult?,
        warnings: MutableList<String>,
        onResult: (SpoilerAnalysisReport) -> Unit
    ) {

        imageAnalyzer.analyze(
            image = image,
            onResult = { imageResult ->

                finishAnalysis(
                    extractedText = extractedText,
                    classification = classification,
                    textResult = textResult,
                    imageResult = imageResult,
                    warnings = warnings,
                    onResult = onResult
                )
            },
            onError = { error ->

                warnings.add(
                    "Image analysis failed: ${error.message ?: "Unknown error"}"
                )

                finishAnalysis(
                    extractedText = extractedText,
                    classification = classification,
                    textResult = textResult,
                    imageResult = null,
                    warnings = warnings,
                    onResult = onResult
                )
            }
        )
    }

    private fun finishAnalysis(
        extractedText: String,
        classification: GtaClassification?,
        textResult: SpoilerResult?,
        imageResult: ImageAnalysisResult?,
        warnings: List<String>,
        onResult: (SpoilerAnalysisReport) -> Unit
    ) {

        val decision = decisionEngine.decide(
            textResult = textResult,
            classification = classification,
            imageResult = imageResult
        )

        val report = SpoilerAnalysisReport(
            decision = decision,
            extractedText = extractedText,
            classification = classification,
            imageAnalysis = imageResult,
            warnings = warnings.toList()
        )

        isProcessing.set(false)
        onResult(report)
    }

    fun close() {
        textAnalyzer.close()
        imageAnalyzer.close()
    }
}
