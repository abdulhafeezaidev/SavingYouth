package com.behavioralfirewall.domain.engine.escalation

import com.behavioralfirewall.domain.engine.InterventionManager
import com.behavioralfirewall.domain.model.InterventionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionTriggerBridge @Inject constructor(
    private val sequenceEngine: EscalationSequenceEngine,
    private val riskEngine: IntentRiskEngine,
    private val interventionManager: InterventionManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        startBridging()
    }

    private fun startBridging() {
        scope.launch {
            sequenceEngine.escalationState.collectLatest { state ->
                val score = riskEngine.riskScore.value
                val intervention = when (state.stage) {
                    EscalationStage.NORMAL -> null
                    // Suspicious triggers a warnings notification or nudge
                    EscalationStage.SUSPICIOUS -> InterventionType.WARNING
                    // Escalating triggers a breathing reset
                    EscalationStage.ESCALATING -> InterventionType.BREATHING
                    // High risk triggers a countdown delay
                    EscalationStage.HIGH_RISK -> InterventionType.DELAY
                    // Critical triggers a reflection prompt
                    EscalationStage.CRITICAL -> InterventionType.REFLECTION_PROMPT
                }

                if (intervention != null) {
                    // Trigger the intervention using the main application intervention system
                    interventionManager.triggerIntervention(
                        type = intervention,
                        triggerPackage = "intent_escalation",
                        riskScore = score
                    )
                }
            }
        }
    }
}
