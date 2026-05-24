package com.behavioralfirewall.domain.engine.escalation

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class KeywordMatch(
    val keyword: String,
    val matchedWord: String,
    val baseScore: Int,
    val confidence: Float,
    val finalScore: Int
)

@Singleton
class KeywordRiskAnalyzer @Inject constructor() {

    // Configurable weighted keyword dictionary
    private val keywordWeights = mapOf(
        "vpn" to 20,
        "proxy" to 15,
        "private browser" to 20,
        "uc browser" to 25,
        "1.1.1.1" to 15,
        "incognito" to 10,
        "tor" to 20,
        "orbot" to 20,
        "unblock" to 15,
        "bypass" to 15
    )

    /**
     * Analyzes raw text input, identifying matched keywords using exact, substring,
     * and fuzzy typo-tolerant matching logic.
     */
    fun analyze(text: String): List<KeywordMatch> {
        val normalized = text.lowercase(Locale.getDefault()).trim()
        if (normalized.isEmpty()) return emptyList()

        val matchedList = mutableListOf<KeywordMatch>()

        // 1. Check full phrase matches first (e.g. "private browser")
        for ((keyword, score) in keywordWeights) {
            if (keyword.contains(" ") && normalized.contains(keyword)) {
                matchedList.add(
                    KeywordMatch(
                        keyword = keyword,
                        matchedWord = keyword,
                        baseScore = score,
                        confidence = 1.0f,
                        finalScore = score
                    )
                )
            }
        }

        // 2. Tokenize text into words for single-word matching
        val words = normalized.split(Regex("[\\s,.:;?\\(\\)\\[\\]\\-_\\/\\\"\\'\\!]+"))
            .filter { it.isNotBlank() }

        for (word in words) {
            // Skip extremely short words to avoid false positive matches
            if (word.length < 3 && word != "tor") continue

            for ((keyword, score) in keywordWeights) {
                // If it was already matched in a phrase, skip
                if (matchedList.any { it.keyword == keyword }) continue
                if (keyword.contains(" ")) continue // Phrase checks already done

                val match = evaluateMatch(word, keyword, score)
                if (match != null) {
                    matchedList.add(match)
                }
            }
        }

        return matchedList
    }

    private fun evaluateMatch(word: String, keyword: String, baseScore: Int): KeywordMatch? {
        // Direct equals match
        if (word == keyword) {
            return KeywordMatch(keyword, word, baseScore, 1.0f, baseScore)
        }

        // Substring check
        if (word.contains(keyword) || keyword.contains(word)) {
            val ratio = minOf(word.length, keyword.length).toFloat() / maxOf(word.length, keyword.length).toFloat()
            if (ratio >= 0.7f) {
                val finalScore = (baseScore * ratio).toInt()
                return KeywordMatch(keyword, word, baseScore, ratio, finalScore)
            }
        }

        // Fuzzy typo check using Levenshtein distance
        val dist = levenshteinDistance(word, keyword)
        val wordLen = word.length
        val kwLen = keyword.length

        // For short words (length 3 or 4), allow distance <= 1
        if (kwLen in 3..4 && dist <= 1) {
            val confidence = 0.8f
            val finalScore = (baseScore * confidence).toInt()
            return KeywordMatch(keyword, word, baseScore, confidence, finalScore)
        }

        // For longer words, allow distance <= 2
        if (kwLen >= 5 && dist <= 2) {
            val confidence = if (dist == 1) 0.8f else 0.6f
            val finalScore = (baseScore * confidence).toInt()
            return KeywordMatch(keyword, word, baseScore, confidence, finalScore)
        }

        return null
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var distance = IntArray(lhsLength + 1) { it }
        var newDistance = IntArray(lhsLength + 1)
        for (i in 1..rhsLength) {
            newDistance[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costInsert = distance[j] + 1
                val costDelete = newDistance[j - 1] + 1
                val costReplace = distance[j - 1] + match
                newDistance[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = distance
            distance = newDistance
            newDistance = swap
        }
        return distance[lhsLength]
    }
}
