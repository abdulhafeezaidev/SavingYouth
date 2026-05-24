package com.behavioralfirewall.presentation.screens.escalation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.behavioralfirewall.domain.engine.escalation.EscalationStage
import com.behavioralfirewall.domain.engine.escalation.SequenceSignal
import com.behavioralfirewall.presentation.theme.PremiumBackgroundGradient
import com.behavioralfirewall.presentation.viewmodel.IntentEscalationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ColorSuccess = Color(0xFF00E676) // Neon Green
private val ColorError = Color(0xFFFF3B30)   // Neon Red
private val ColorWarning = Color(0xFFFF9100) // Neon Orange
private val ColorInfo = Color(0xFF29B6F6)    // Neon Blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentEscalationDashboardScreen(
    viewModel: IntentEscalationViewModel,
    onBack: () -> Unit
) {
    val riskScore by viewModel.riskScore.collectAsState()
    val escalationState by viewModel.escalationState.collectAsState()
    val activeSignals by viewModel.activeSignals.collectAsState()
    val falsePositiveEvents by viewModel.falsePositiveEvents.collectAsState()
    val keywordMatchHistory by viewModel.keywordMatchHistory.collectAsState()

    var simulatorText by remember { mutableStateOf("") }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intent Escalation Center", fontWeight = FontWeight.Bold) },
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PremiumBackgroundGradient)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Live Escalation State & Score Dial
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Dynamic Intent Scoring",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(150.dp)
                            ) {
                                val scoreColor = when {
                                    riskScore >= 75 -> ColorError
                                    riskScore >= 50 -> ColorWarning
                                    riskScore >= 25 -> ColorInfo
                                    else -> ColorSuccess
                                }

                                CircularProgressIndicator(
                                    progress = { riskScore.toFloat() / 100f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = scoreColor,
                                    strokeWidth = 10.dp,
                                    trackColor = MaterialTheme.colorScheme.outline
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$riskScore",
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Risk Level",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val stageColor = when (escalationState.stage) {
                                EscalationStage.NORMAL -> ColorSuccess
                                EscalationStage.SUSPICIOUS -> ColorInfo
                                EscalationStage.ESCALATING -> ColorWarning
                                EscalationStage.HIGH_RISK -> ColorError
                                EscalationStage.CRITICAL -> ColorError
                            }

                            Text(
                                text = "Escalation Stage: ${escalationState.stage.name}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = stageColor
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = escalationState.explanation,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                // 2. Interactive Simulator Panel
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Escalation Simulator (Sandbox)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = simulatorText,
                                onValueChange = { simulatorText = it },
                                label = { Text("Simulate user typing keyword") },
                                placeholder = { Text("e.g. vpn, proxy, npm setup") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (simulatorText.isNotBlank()) {
                                        viewModel.simulateTyping(simulatorText)
                                        simulatorText = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Trigger Keystroke Event", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Sequence Signals Injection",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.simulatePlayStoreLateNight() },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                                ) {
                                    Text("Play Store Late Night", fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                                Button(
                                    onClick = { viewModel.simulateVpnLaunch() },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                                ) {
                                    Text("VPN App Open", fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                                Button(
                                    onClick = { viewModel.simulateBrowserLaunch() },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                                ) {
                                    Text("Browser Open", fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.triggerDecay() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                                ) {
                                    Text("Force Decay (-10)", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { viewModel.clearAll() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                                ) {
                                    Text("Clear All Signals", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. Active Signals sliding window (Queue)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Sliding Window Signals (Last 15m)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (activeSignals.isEmpty()) {
                                Text(
                                    text = "No active sequence signals detected.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                activeSignals.forEach { signal ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = signal.description,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Signal: ${signal.type.name} • ${timeFormatter.format(Date(signal.timestamp))}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Match & Privacy Redaction Logs
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Privacy-Safe Keyword Matches",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Note: Raw typed keystrokes are discarded instantly. Only categorized match metadata is logged.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (keywordMatchHistory.isEmpty()) {
                                Text(
                                    text = "No keyword matches logged.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                keywordMatchHistory.forEach { log ->
                                    Text(
                                        text = log,
                                        fontSize = 12.sp,
                                        color = ColorInfo,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. False Positive Reduction Events
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "False Positive Reduction Events",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (falsePositiveEvents.isEmpty()) {
                                Text(
                                    text = "No false positive reduction events logged.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                falsePositiveEvents.forEach { event ->
                                    Text(
                                        text = event,
                                        fontSize = 12.sp,
                                        color = ColorSuccess,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
