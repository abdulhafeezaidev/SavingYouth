package com.behavioralfirewall.domain.model

enum class InterventionState {
    IDLE,
    MONITORING,
    WARNING,
    INTERRUPTING,
    COOLDOWN,
    ESCALATED
}
