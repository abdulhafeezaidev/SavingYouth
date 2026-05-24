package com.behavioralfirewall.domain.engine

import kotlinx.coroutines.flow.Flow

interface RiskEngine {
    fun calculateRiskScore(
        packageName: String,
        sessionDurationMs: Long,
        switchCount: Int,
        stressLevel: Int
    ): Int
}
