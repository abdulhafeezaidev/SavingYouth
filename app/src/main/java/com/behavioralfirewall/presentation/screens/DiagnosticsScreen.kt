package com.behavioralfirewall.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.behavioralfirewall.domain.engine.BatteryOptimizationMode
import com.behavioralfirewall.domain.launch.AppEnvironment
import com.behavioralfirewall.domain.launch.FeedbackCategory
import com.behavioralfirewall.domain.evolution.*
import com.behavioralfirewall.presentation.viewmodel.DiagnosticsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ColorSuccess = Color(0xFF00E676) // Vibrant Cyber Green
private val ColorError = Color(0xFFFF3B30)   // Vibrant Cyber Red
private val ColorWarning = Color(0xFFFF9100) // Vibrant Cyber Orange
private val ColorInfo = Color(0xFF29B6F6)    // Vibrant Cyber Blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel,
    onBack: () -> Unit,
    onNavigateToEscalationDashboard: () -> Unit
) {
    val healthState by viewModel.healthState.collectAsState()
    val diagnosticLogs by viewModel.diagnosticLogs.collectAsState()
    val systemHealthPercentage by viewModel.systemHealthPercentage.collectAsState()
    val batteryMode by viewModel.batteryMode.collectAsState()
    val currentIntervalMs by viewModel.currentIntervalMs.collectAsState()
    val trackedOverlaysCount by viewModel.trackedOverlaysCount.collectAsState()
    val memoryState by viewModel.memoryState.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val lastCrashReason by viewModel.lastCrashReason.collectAsState()

    // Rollout & Deployment State
    val currentEnvironment by viewModel.currentEnvironment.collectAsState()
    val rolloutPercentage by viewModel.rolloutPercentage.collectAsState()
    val isRollbackActive by viewModel.isRollbackActive.collectAsState()
    val userSeed = viewModel.userRolloutSeed

    // Kill Switches State
    val isGlobalSystemKilled by viewModel.isGlobalSystemKilled.collectAsState()
    val isVpnModuleKilled by viewModel.isVpnModuleKilled.collectAsState()
    val isOverlayKilled by viewModel.isOverlayKilled.collectAsState()
    val isAccessibilityKilled by viewModel.isAccessibilityKilled.collectAsState()

    // Stability Guard State
    val stabilityMessage by viewModel.stabilityMessage.collectAsState()

    // Telemetry State
    val crashRate by viewModel.crashRate.collectAsState()
    val anrRate by viewModel.anrRate.collectAsState()
    val vpnFailureRate by viewModel.vpnFailureRate.collectAsState()
    val interventionFailureRate by viewModel.interventionFailureRate.collectAsState()
    val serviceUptimePercent by viewModel.serviceUptimePercent.collectAsState()

    // Feedback State
    val feedbackList by viewModel.feedbackList.collectAsState()
    val isSubmittingFeedback by viewModel.isSubmittingFeedback.collectAsState()

    // === EVOLUTION STATES ===
    val activeWeights by viewModel.activeWeights.collectAsState()
    val modelVersion by viewModel.modelVersion.collectAsState()
    val systemAccuracy by viewModel.systemAccuracy.collectAsState()
    val falsePositiveRate by viewModel.falsePositiveRate.collectAsState()
    val isEvolutionRecommended by viewModel.isEvolutionRecommended.collectAsState()
    val driftReport by viewModel.driftReport.collectAsState()
    
    val fatigueLevel by viewModel.fatigueLevel.collectAsState()
    val dynamicCooldownMultiplier by viewModel.dynamicCooldownMultiplier.collectAsState()
    val recommendedInterventionStyle by viewModel.recommendedInterventionStyle.collectAsState()

    val sensitivityDiscount by viewModel.sensitivityDiscount.collectAsState()
    val safeSessionsCount by viewModel.safeSessionsCount.collectAsState()

    val activeSegment by viewModel.activeSegment.collectAsState()
    val activeExperimentGroup by viewModel.activeExperimentGroup.collectAsState()

    // UI Input states
    var feedbackText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FeedbackCategory.BUG_REPORT) }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Evolution Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("< Back", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Dashboard Top Indicators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ADAPTIVE INTELLIGENCE ACCURACY",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.1f%%", systemAccuracy),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("False Positives", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text(String.format("%.1f%%", falsePositiveRate), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Model Version", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text(modelVersion.substringBefore("-"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Intent Escalation Engine Developer Hub
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    onClick = onNavigateToEscalationDashboard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Intent Escalation Center \u2192",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "View live sequence stages, keywords timeline, and interact with intent simulators.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // A. Post-Launch Adaptive Sensitivity & Streaks Simulation
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sensitivity & Fatigue Simulator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        TelemetryMetricRow("Safe Activity Streak", "$safeSessionsCount sessions", ColorSuccess)
                        TelemetryMetricRow("Sensitivity Factor", String.format("%.2fx", sensitivityDiscount), if (sensitivityDiscount < 1.0f) ColorSuccess else MaterialTheme.colorScheme.onSurface)
                        TelemetryMetricRow("Alert Fatigue Scale", fatigueLevel.name, if (fatigueLevel == FatigueLevel.LOW) ColorSuccess else ColorError)
                        TelemetryMetricRow("Cooldown Multiplier", String.format("%.1fx", dynamicCooldownMultiplier), if (dynamicCooldownMultiplier > 1.0f) ColorError else MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Interactive Progress Simulations", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.simulateSafeUsageSession() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess)
                            ) {
                                Text("Safe Session", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.simulateHighRiskFailure() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                            ) {
                                Text("Relapse Spill", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.resetEvolutionMetrics() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Reset Metrics", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // B. User Segmentation Dashboard
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dynamic User Segmentation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            val segmentColor = when (activeSegment) {
                                UserSegment.HIGH_RISK -> ColorError
                                UserSegment.MODERATE_RISK -> ColorWarning
                                UserSegment.RECOVERY_STAGE -> MaterialTheme.colorScheme.primary
                                UserSegment.STABLE -> ColorSuccess
                            }
                            Box(
                                modifier = Modifier
                                    .background(segmentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = activeSegment.name,
                                    color = segmentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val threshold = when (activeSegment) {
                            UserSegment.HIGH_RISK -> 45
                            UserSegment.MODERATE_RISK -> 60
                            UserSegment.RECOVERY_STAGE -> 70
                            UserSegment.STABLE -> 80
                        }

                        val notificationStyle = when (activeSegment) {
                            UserSegment.HIGH_RISK -> "STRICT_URGENT"
                            UserSegment.MODERATE_RISK -> "DISCREET_STANDARD"
                            UserSegment.RECOVERY_STAGE -> "DISCREET_SUPPORTIVE"
                            UserSegment.STABLE -> "SILENT_MINIMAL"
                        }

                        val interactionStyle = when (activeSegment) {
                            UserSegment.HIGH_RISK -> "STRICT_SCREEN_LOCK"
                            UserSegment.MODERATE_RISK -> "REFLECTION_PROMPT"
                            UserSegment.RECOVERY_STAGE -> "COGNITIVE_FRICTION"
                            UserSegment.STABLE -> "AMBIENT_NOTIFICATION"
                        }

                        TelemetryMetricRow("Adapted Scoring Threshold", "$threshold risk limit", Color.Gray)
                        TelemetryMetricRow("Adaptive Notification Style", notificationStyle, Color.Gray)
                        TelemetryMetricRow("Intervention Visual Overlay", interactionStyle, Color.Gray)
                    }
                }
            }

            // C. Behavioral Model Evolution & Safety Weights
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Model Scoring Parameter Evolution",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = modelVersion,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        TelemetryMetricRow("Active Urgency Weight", String.format("%.2f", activeWeights.urgencyWeight))
                        TelemetryMetricRow("Screen Switch Weight", String.format("%.2f", activeWeights.screenChangeWeight))
                        TelemetryMetricRow("Dwell Duration Weight", String.format("%.2f", activeWeights.dwellTimeWeight))
                        TelemetryMetricRow("Core Alert Threshold Limit", "${activeWeights.coreThreshold} risk limit")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Secure Parameter Manual Testing", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerWeightEvolution(0.8f, 0.4f, 0.3f, 50) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Tune Aggressive", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.triggerWeightEvolution(0.3f, 0.1f, 0.4f, 75) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Tune Conservative", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.triggerWeightRollback() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                            ) {
                                Text("Rollback default", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // D. Post-Launch Drift Detection & Auto-Calibration
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Model Drift Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isEvolutionRecommended) ColorWarning.copy(alpha = 0.1f) else ColorSuccess.copy(alpha = 0.1f),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isEvolutionRecommended) ColorWarning else ColorSuccess,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isEvolutionRecommended) "ACCURACY DRIFT ALERT" else "INTELLIGENCE ACCURACY SECURE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isEvolutionRecommended) ColorWarning else ColorSuccess
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = driftReport,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (isEvolutionRecommended) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.applyRecommendedWeights() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorWarning)
                            ) {
                                Text("Apply Recommended Calibrated Weights")
                            }
                        }
                    }
                }
            }

            // E. Dynamic A/B Testing Management
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "A/B Testing Experiments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .background(ColorInfo.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = activeExperimentGroup.name,
                                    color = ColorInfo,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        val overlayStyle = when (activeExperimentGroup) {
                            ExperimentGroup.CONTROL -> "WARNING_DIALOG"
                            ExperimentGroup.EXPERIMENTAL_INTERACTIVE -> "BREATHING_REFLECTION"
                            ExperimentGroup.EXPERIMENTAL_DELAY -> "PROGRESSIVE_COUNTDOWN"
                        }
                        
                        val onboardingStyle = when (activeExperimentGroup) {
                            ExperimentGroup.CONTROL -> "4-Step Standard"
                            ExperimentGroup.EXPERIMENTAL_INTERACTIVE -> "3-Step Fast Conversion"
                            ExperimentGroup.EXPERIMENTAL_DELAY -> "5-Step Comprehensive"
                        }

                        TelemetryMetricRow("Intervention UI variant", overlayStyle)
                        TelemetryMetricRow("Onboarding steps variant", onboardingStyle)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Test and Shift Cohorts manually", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExperimentGroup.values().forEach { group ->
                                Button(
                                    onClick = { viewModel.forceExperimentCohort(group) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeExperimentGroup == group) ColorInfo else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = group.name.replace("EXPERIMENTAL_", "").take(7),
                                        fontSize = 9.sp,
                                        color = if (activeExperimentGroup == group) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // === F. ORIGINAL TELEMETRY, ROLLOUT, FEEDBACK & LOGS SECTION ===
            item {
                Text(
                    text = "Release, Rollout & Core Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Original Release & Staged Rollout Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Release Deployment Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Active App Environment", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppEnvironment.values().forEach { env ->
                                val selected = currentEnvironment == env
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setEnvironment(env) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = env.name,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Staged Rollout Gate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("$rolloutPercentage%", fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = rolloutPercentage.toFloat(),
                            onValueChange = { viewModel.updateRolloutPercentage(it.toInt()) },
                            valueRange = 0f..100f,
                            steps = 10,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Local User Seed: $userSeed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val allocated = userSeed < rolloutPercentage && !isRollbackActive
                                Text(
                                    text = if (isRollbackActive) "State: ROLLED BACK" else if (allocated) "Status: IN STAGE (Active)" else "Status: COMPATIBLE ONLY (Inactive)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRollbackActive) ColorError else if (allocated) ColorSuccess else ColorWarning
                                )
                            }

                            if (isRollbackActive) {
                                Button(
                                    onClick = { viewModel.clearEmergencyRollback() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Resume", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.triggerEmergencyRollback() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Emergency Rollback", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Original Stability Guard
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stability Guard Monitor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (stabilityMessage.startsWith("CRITICAL") || stabilityMessage.startsWith("ALERT")) ColorError.copy(alpha = 0.1f) else ColorSuccess.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (stabilityMessage.startsWith("System")) "STABLE" else "PROTECTED",
                                    color = if (stabilityMessage.startsWith("System")) ColorSuccess else ColorError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stabilityMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (stabilityMessage.startsWith("System")) MaterialTheme.colorScheme.onSurface else ColorError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Simulate In-Wild Failures (Stability Audit)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.simulateStabilityOverlayTrigger() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Overlay Spike", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.simulateStabilityVpnFailure() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("VPN Failure", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.resetGuardStats() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess)
                            ) {
                                Text("Clear Guard", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Original Emergency Overrides & Kill Switches
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Emergency Remote Kill Switches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        KillSwitchRow("Global System Disable", isGlobalSystemKilled) {
                            viewModel.setKillSwitch("global", it)
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        KillSwitchRow("VPN Filter Disable", isVpnModuleKilled) {
                            viewModel.setKillSwitch("vpn", it)
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        KillSwitchRow("Overlay UI Disable", isOverlayKilled) {
                            viewModel.setKillSwitch("overlay", it)
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        KillSwitchRow("Accessibility Lock Disable", isAccessibilityKilled) {
                            viewModel.setKillSwitch("accessibility", it)
                        }
                    }
                }
            }

            // Original In-Wild Observability & Live Telemetry
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Production Telemetry Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        TelemetryMetricRow("Simulated Uptime", String.format("%.2f%%", serviceUptimePercent), ColorSuccess)
                        TelemetryMetricRow("App Crash Rate", String.format("%.2f%%", crashRate), if (crashRate > 2.0f) ColorError else MaterialTheme.colorScheme.onSurface)
                        TelemetryMetricRow("ANR Loop Rate", String.format("%.2f%%", anrRate), if (anrRate > 1.0f) ColorError else MaterialTheme.colorScheme.onSurface)
                        TelemetryMetricRow("VPN Drop Frequency", String.format("%.2f%%", vpnFailureRate), if (vpnFailureRate > 5.0f) ColorError else MaterialTheme.colorScheme.onSurface)
                        TelemetryMetricRow("Intervention Misfire", String.format("%.2f%%", interventionFailureRate), if (interventionFailureRate > 10.0f) ColorError else MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Simulate Live Telemetry Events", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.simulateAppLaunch() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("Launch App", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Button(
                                onClick = { viewModel.simulateCrashReport() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("Report Crash", fontSize = 11.sp, color = ColorError)
                            }
                            Button(
                                onClick = { viewModel.simulateAnrReport() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("Report ANR", fontSize = 11.sp, color = ColorWarning)
                            }
                        }
                    }
                }
            }

            // Original In-App Anonymous User Feedback Loop
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Anonymous User Feedback Loop",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Redacts URLs, emails, and package names automatically to guarantee no sensitive behavior leaks.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Category", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FeedbackCategory.values().forEach { cat ->
                                val selected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.name.replace("_", " "),
                                        color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("Feedback Text (redaction is active)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (feedbackText.isNotBlank()) {
                                    viewModel.submitFeedback(selectedCategory, feedbackText)
                                    feedbackText = ""
                                }
                            },
                            enabled = !isSubmittingFeedback && feedbackText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Anonymously")
                        }
                    }
                }
            }

            // Feedback logs compiled
            if (feedbackList.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Redacted Feedback Logs (${feedbackList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Clear Logs",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.clearFeedbackLogs() }
                        )
                    }
                }

                items(feedbackList.reversed()) { feed ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        feed.category.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = timeFormatter.format(Date(feed.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(feed.sanitizedText, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Device: ${feed.deviceModel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Original Services Active State Rows
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Services Internal Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ServiceStatusRow("Accessibility Monitoring Service", healthState.accessibilityServiceActive)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        ServiceStatusRow("Safe Browsing VPN Service", healthState.vpnServiceActive)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        ServiceStatusRow("Intervention Overlay Service", healthState.overlayServiceActive)
                    }
                }
            }

            // Original Optimization & Battery Efficiency Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Battery Optimization State",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Mode", style = MaterialTheme.typography.bodyLarge)
                            val modeText = when (batteryMode) {
                                BatteryOptimizationMode.LIGHTWEIGHT -> "Lightweight (Eco)"
                                BatteryOptimizationMode.BALANCED -> "Balanced"
                                BatteryOptimizationMode.AGGRESSIVE -> "Aggressive (High Alert)"
                            }
                            val modeColor = when (batteryMode) {
                                BatteryOptimizationMode.LIGHTWEIGHT -> ColorSuccess
                                BatteryOptimizationMode.BALANCED -> MaterialTheme.colorScheme.primary
                                BatteryOptimizationMode.AGGRESSIVE -> ColorError
                            }
                            Text(modeText, color = modeColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Evaluation Loop", style = MaterialTheme.typography.bodyLarge)
                            Text("${currentIntervalMs / 1000}s interval", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Original Memory Stability Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Memory Stability",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Memory Pressure State", style = MaterialTheme.typography.bodyLarge)
                            Text(memoryState, fontWeight = FontWeight.Bold, color = if (memoryState == "Normal") ColorSuccess else ColorError)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tracked Overlays", style = MaterialTheme.typography.bodyLarge)
                            Text("$trackedOverlaysCount active", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.triggerEmergencyCleanup() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Trigger Emergency Memory GC")
                        }
                    }
                }
            }

            // Original Diagnostics Logs Header
            item {
                Text(
                    text = "System Diagnostics Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Original List of Logs
            items(diagnosticLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val timeStr = timeFormatter.format(Date(log.timestamp))
                            Text(
                                text = "[$timeStr] ${log.category}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (log.category) {
                                    "CRASH" -> ColorError
                                    "LATENCY" -> ColorWarning
                                    "MEMORY" -> ColorInfo
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KillSwitchRow(
    label: String,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = if (isActive) "ACTIVE (MODULE DISABLED)" else "Inactive (Module Running)",
                fontSize = 11.sp,
                color = if (isActive) ColorError else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ColorError,
                checkedTrackColor = ColorError.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun TelemetryMetricRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun ServiceStatusRow(
    name: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge)
        val statusText = if (isActive) "ACTIVE" else "INACTIVE"
        val statusColor = if (isActive) ColorSuccess else ColorError
        Box(
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                statusText,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
