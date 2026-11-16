
package com.gtashield.ai

enum class SpoilerAction {
    BLOCK,
    ALLOW,
    REVIEW
}

data class SpoilerDecision(
    val action: SpoilerAction,
    val reason: String,
    val evidence: List<String>
)

class SpoilerDecisionEngine {

    fun decide(
        textResult: SpoilerResult?,
        classification: GtaClassification?,
        imageResult: ImageAnalysisResult?
    ): SpoilerDecision {

        val evidence = mutableListOf<String>()

        // دليل من تحليل النص OCR
        if (textResult?.detected == true) {
            evidence.addAll(textResult.matchedKeywords)
        }

        // دليل من تصنيف النص
        if (classification?.isGtaContent == true) {
            evidence.addAll(classification.matchedTerms)
        }

        // وجود دليل نصي واضح على GTA 6
        if (evidence.isNotEmpty()) {
            return SpoilerDecision(
                action = SpoilerAction.BLOCK,
                reason = "GTA 6 content detected in text",
                evidence = evidence.distinct()
            )
        }

        // الصورة قد تشير إلى لعبة، لكنها لا تثبت أنها GTA 6
        if (imageResult?.containsGamingContent == true) {
            return SpoilerDecision(
                action = SpoilerAction.REVIEW,
                reason = "Gaming content detected, GTA 6 not confirmed",
                evidence = imageResult.labels
            )
        }

        return SpoilerDecision(
            action = SpoilerAction.ALLOW,
            reason = "No sufficient GTA 6 evidence detected",
            evidence = emptyList()
        )
    }
}
