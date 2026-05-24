package com.behavioralfirewall.domain.model

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class RiskState(
    val score: Int = 0,
    val level: RiskLevel = RiskLevel.LOW,
    val activeTriggers: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
