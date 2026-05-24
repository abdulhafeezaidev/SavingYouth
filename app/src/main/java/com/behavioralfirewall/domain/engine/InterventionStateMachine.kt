package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.domain.model.InterventionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionStateMachine @Inject constructor() {
    private val _state = MutableStateFlow(InterventionState.IDLE)
    val state: StateFlow<InterventionState> = _state.asStateFlow()

    @Synchronized
    fun transitionTo(newState: InterventionState): Boolean {
        val currentState = _state.value
        
        // Prevent redundant transitions
        if (currentState == newState) return false
        
        // Guard transition validity to prevent loop cycles and overlapping issues
        val isAllowed = when (currentState) {
            InterventionState.IDLE -> newState == InterventionState.MONITORING
            
            InterventionState.MONITORING -> newState in listOf(
                InterventionState.WARNING,
                InterventionState.INTERRUPTING,
                InterventionState.COOLDOWN,
                InterventionState.IDLE
            )
            
            InterventionState.WARNING -> newState in listOf(
                InterventionState.INTERRUPTING,
                InterventionState.COOLDOWN,
                InterventionState.MONITORING,
                InterventionState.ESCALATED
            )
            
            InterventionState.INTERRUPTING -> newState in listOf(
                InterventionState.COOLDOWN,
                InterventionState.ESCALATED,
                InterventionState.MONITORING
            )
            
            InterventionState.COOLDOWN -> newState in listOf(
                InterventionState.MONITORING,
                InterventionState.WARNING,
                InterventionState.INTERRUPTING,
                InterventionState.ESCALATED,
                InterventionState.IDLE
            )
            
            InterventionState.ESCALATED -> newState in listOf(
                InterventionState.COOLDOWN,
                InterventionState.MONITORING,
                InterventionState.IDLE
            )
        }
        
        if (isAllowed) {
            _state.value = newState
            return true
        }
        return false
    }

    fun getCurrentState(): InterventionState = _state.value
}
