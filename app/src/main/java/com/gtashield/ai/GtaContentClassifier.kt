
package com.gtashield.ai

import java.util.Locale

enum class GtaContentType {
    GAME,
    GAMEPLAY,
    LEAK,
    SPOILER,
    TRAILER,
    LIVESTREAM,
    NEWS,
    CHARACTER,
    UNKNOWN
}

data class GtaClassification(
    val isGtaContent: Boolean,
    val category: GtaContentType,
    val confidence: Float,
    val matchedTerms: List<String>
)

class GtaContentClassifier {

    private val categories = mapOf(
        GtaContentType.LEAK to listOf(
            "gta 6 leak",
            "gta 6 leaks",
            "gta vi leak",
            "leaked gameplay",
            "gta 6 leaked footage"
        ),

        GtaContentType.SPOILER to listOf(
            "gta 6 spoiler",
            "gta 6 ending",
            "gta 6 story",
            "gta 6 mission",
            "story ending"
        ),

        GtaContentType.TRAILER to listOf(
            "gta 6 trailer",
            "gta vi trailer",
            "official trailer",
            "new trailer"
        ),

        GtaContentType.LIVESTREAM to listOf(
            "gta 6 live",
            "gta 6 livestream",
            "gta 6 stream",
            "live gta 6"
        ),

        GtaContentType.GAMEPLAY to listOf(
            "gta 6 gameplay",
            "gta vi gameplay",
            "gameplay gta 6",
            "gta 6 walkthrough"
        ),

        GtaContentType.CHARACTER to listOf(
            "lucia gta",
            "jason gta",
            "lucia and jason"
        ),

        GtaContentType.NEWS to listOf(
            "gta 6 news",
            "gta vi news",
            "gta 6 update",
            "rockstar gta 6"
        ),

        GtaContentType.GAME to listOf(
            "gta 6",
            "gta6",
            "gta vi",
            "grand theft auto 6",
            "grand theft auto vi"
        )
    )

    fun classify(text: String): GtaClassification {

        val normalized = text
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val matches = categories.mapValues { (_, terms) ->
            terms.filter { term ->
                Regex(
                    "(^| )" + Regex.escape(term) + "( |$)"
                ).containsMatchIn(normalized)
            }
        }.filterValues { it.isNotEmpty() }

        if (matches.isEmpty()) {
            return GtaClassification(
                isGtaContent = false,
                category = GtaContentType.UNKNOWN,
                confidence = 0f,
                matchedTerms = emptyList()
            )
        }

        val best = matches.maxByOrNull { (_, terms) ->
            terms.size
        }

        return GtaClassification(
            isGtaContent = true,
            category = best?.key ?: GtaContentType.UNKNOWN,
            confidence = 0.85f,
            matchedTerms = matches.values.flatten().distinct()
        )
    }
}
