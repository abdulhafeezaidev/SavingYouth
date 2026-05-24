package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.domain.model.InterventionType
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionRecommendationEngine @Inject constructor(
    private val repository: BehavioralRepository
) {
    suspend fun getRecommendedIntervention(defaultType: InterventionType): InterventionType {
        val events = repository.getRiskEvents().firstOrNull() ?: return defaultType
        
        val resolvedEvents = events.filter { 
            it.interventionType != "NONE" && 
            it.outcome in listOf("complied", "dismissed") 
        }
        
        if (resolvedEvents.isEmpty()) return defaultType

        // Group by type and calculate compliance rate
        val complianceRates = resolvedEvents.groupBy { it.interventionType }
            .mapValues { (_, list) ->
                val complied = list.count { it.outcome == "complied" }
                complied.toFloat() / list.size.toFloat()
            }

        // If default intervention type performance is low (< 35%), dynamically recommend the highest compliant type
        val defaultRate = complianceRates[defaultType.name] ?: 1.0f
        if (defaultRate < 0.35f) {
            val bestTypeString = complianceRates.maxByOrNull { it.value }?.key
            if (bestTypeString != null) {
                try {
                    return InterventionType.valueOf(bestTypeString)
                } catch (e: Exception) {
                    // Fallback to default
                }
            }
        }

        return defaultType
    }
}
