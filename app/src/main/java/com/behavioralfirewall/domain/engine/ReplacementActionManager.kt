package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.domain.model.ReplacementAction
import com.behavioralfirewall.domain.model.ReplacementActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReplacementActionManager @Inject constructor() {
    private val _actions = MutableStateFlow<List<ReplacementAction>>(emptyList())
    val actions: StateFlow<List<ReplacementAction>> = _actions.asStateFlow()

    init {
        generateDefaultActions()
    }

    private fun generateDefaultActions() {
        val list = listOf(
            ReplacementAction(
                type = ReplacementActionType.BREATHING,
                title = "Calming Breathing Reset",
                description = "Follow the visual breathing circle. Complete a 1-minute box breathing session.",
                durationSeconds = 60
            ),
            ReplacementAction(
                type = ReplacementActionType.PUSHUPS,
                title = "Re-centering Pushups",
                description = "Drop down and do 10 pushups to release physical tension.",
                durationSeconds = 30
            ),
            ReplacementAction(
                type = ReplacementActionType.HYDRATION,
                title = "Mindful Hydration",
                description = "Drink a full glass of cool water slowly. Notice the physical sensation.",
                durationSeconds = 15
            ),
            ReplacementAction(
                type = ReplacementActionType.WALK,
                title = "5-Minute Grounding Walk",
                description = "Walk slowly for 5 minutes. Notice your feet touching the ground.",
                durationSeconds = 300
            ),
            ReplacementAction(
                type = ReplacementActionType.STUDY_SPRINT,
                title = "5-Minute Study Sprint",
                description = "Dive into your current learning or productive task for a brief focus block.",
                durationSeconds = 300
            ),
            ReplacementAction(
                type = ReplacementActionType.JOURNALING,
                title = "Calming Gratitude Prompt",
                description = "Quickly list 3 distinct things you are grateful for right now.",
                durationSeconds = 45
            )
        )
        _actions.value = list
    }

    fun completeAction(type: ReplacementActionType) {
        _actions.value = _actions.value.map { action ->
            if (action.type == type) {
                action.copy(isCompleted = true, completedAt = System.currentTimeMillis())
            } else {
                action
            }
        }
    }

    fun resetActions() {
        generateDefaultActions()
    }

    fun getAction(type: ReplacementActionType): ReplacementAction? {
        return _actions.value.find { it.type == type }
    }
}
