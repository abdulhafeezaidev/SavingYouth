package com.behavioralfirewall.domain.model

enum class ReplacementActionType {
    BREATHING,
    PUSHUPS,
    HYDRATION,
    WALK,
    STUDY_SPRINT,
    JOURNALING
}

data class ReplacementAction(
    val type: ReplacementActionType,
    val title: String,
    val description: String,
    val durationSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
