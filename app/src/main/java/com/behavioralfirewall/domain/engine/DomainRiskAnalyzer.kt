package com.behavioralfirewall.domain.engine

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainRiskAnalyzer @Inject constructor() {
    private val blockedAdultDomains = listOf(
        "explicitwebsite.com",
        "badmirrordomain.org",
        "riskycontent.net",
        "adultdomain.com"
    )

    private val riskySearchKeywords = listOf(
        "how to relapse",
        "bypass firewall app",
        "explicit video",
        "adult photos"
    )

    fun isDomainBlocked(domain: String): Pair<Boolean, String> {
        val cleanDomain = domain.lowercase().trim()
        
        // 1. Direct category match
        for (blocked in blockedAdultDomains) {
            if (cleanDomain == blocked || cleanDomain.endsWith(".$blocked")) {
                return Pair(true, "adult content")
            }
        }

        // 2. Fuzzy matching (Levenshtein distance check for suspicious domain variations)
        for (blocked in blockedAdultDomains) {
            val cleanPrefix = cleanDomain.substringBefore(".")
            val blockedPrefix = blocked.substringBefore(".")
            if (cleanPrefix.length > 3 && calculateLevenshteinDistance(cleanPrefix, blockedPrefix) <= 1) {
                return Pair(true, "suspicious mirror domain variation")
            }
        }

        return Pair(false, "safe")
    }

    fun isRiskySearchIntent(query: String): Boolean {
        val cleanQuery = query.lowercase().trim()
        for (keyword in riskySearchKeywords) {
            if (cleanQuery.contains(keyword)) {
                return true
            }
        }
        return false
    }

    private fun calculateLevenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val len0 = lhs.length + 1
        val len1 = rhs.length + 1
        var cost = IntArray(len0)
        var newcost = IntArray(len0)
        for (i in 0 until len0) cost[i] = i
        for (j in 1 until len1) {
            newcost[0] = j
            for (i in 1 until len0) {
                val match = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                val costReplace = cost[i - 1] + match
                val costInsert = cost[i] + 1
                val costDelete = newcost[i - 1] + 1
                newcost[i] = Math.min(Math.min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newcost
            newcost = swap
        }
        return cost[len0 - 1]
    }
}
