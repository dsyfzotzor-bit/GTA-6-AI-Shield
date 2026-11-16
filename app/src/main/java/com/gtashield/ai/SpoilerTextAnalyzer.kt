
package com.gtashield.ai

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

data class SpoilerResult(
    val detected: Boolean,
    val extractedText: String,
    val matchedKeywords: List<String>
)

class SpoilerTextAnalyzer {

    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    private val isProcessing = AtomicBoolean(false)

    private val keywords = listOf(
        "gta 6",
        "gta vi",
        "gta6",
        "gta six",
        "grand theft auto 6",
        "grand theft auto vi",
        "gta 6 leak",
        "gta 6 spoiler",
        "gta 6 gameplay",
        "gta 6 trailer",
        "gta 6 ending",
        "gta 6 story",
        "lucia gta",
        "jason gta",
        "gta 6 map"
    )

    fun analyze(
        image: InputImage,
        onResult: (SpoilerResult) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {

        if (!isProcessing.compareAndSet(false, true)) {
            return
        }

        recognizer.process(image)
            .addOnSuccessListener { result ->

                val extracted = result.text
                val normalized = extracted
                    .lowercase(Locale.ROOT)
                    .replace(Regex("[^a-z0-9 ]"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()

                val matches = keywords.filter { keyword ->
                    normalized.contains(keyword)
                }

                onResult(
                    SpoilerResult(
                        detected = matches.isNotEmpty(),
                        extractedText = extracted,
                        matchedKeywords = matches
                    )
                )
            }
            .addOnFailureListener { error ->
                onError(error)
            }
            .addOnCompleteListener {
                isProcessing.set(false)
            }
    }

    fun close() {
        recognizer.close()
    }
}
