package com.behavioralfirewall.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.behavioralfirewall.domain.model.RiskLevel
import com.behavioralfirewall.presentation.components.FloatingBottomNavigation
import com.behavioralfirewall.presentation.components.GlassmorphicCard
import com.behavioralfirewall.presentation.theme.GlassBorderBrush
import com.behavioralfirewall.presentation.theme.GlassCardBackgroundBrush
import com.behavioralfirewall.presentation.theme.LocalRadius
import com.behavioralfirewall.presentation.theme.LocalSpacing
import com.behavioralfirewall.presentation.theme.PremiumBackgroundGradient
import com.behavioralfirewall.presentation.viewmodel.HomeDashboardViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    navController: NavController,
    viewModel: HomeDashboardViewModel = hiltViewModel()
) {
    val riskState by viewModel.riskState.collectAsState()
    val activeApp by viewModel.activeApp.collectAsState()
    val isVpnActive by viewModel.isVpnActive.collectAsState()
    val blockedAttemptsToday by viewModel.blockedAttemptsToday.collectAsState()
    val cooldownActive by viewModel.cooldownActive.collectAsState()
    val cooldownTimeRemaining by viewModel.cooldownTimeRemaining.collectAsState()
    val integrityState by viewModel.integrityState.collectAsState()
    val repairNeeded by viewModel.repairNeeded.collectAsState()

    val spacing = LocalSpacing.current
    val radius = LocalRadius.current

    // Infinite breathing pulse for risk score circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val scrollState = rememberScrollState()

    // Emotionally supportive wellness feedback messages
    val encouragementMessage = remember(riskState.level) {
        when (riskState.level) {
            RiskLevel.LOW -> "You are doing beautifully. Your mind is quiet, and you are in control."
            RiskLevel.MEDIUM -> "A small wave is rising. Pause, take a deep breath, and let it pass."
            RiskLevel.HIGH -> "You are experiencing high stress. Remember: you have the power to redirect."
            RiskLevel.CRITICAL -> "This is a moment of challenge. Pause right now. Ground yourself."
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumBackgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small, bottom = spacing.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = "Shield Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(spacing.small))
                    Text(
                        text = "WELLBEING SANCTUARY",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { navController.navigate("statistics") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Go to Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(spacing.small))

                    IconButton(
                        onClick = { navController.navigate("accountability") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Go to Accountability",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Abnormal shutdown repair notice
            if (repairNeeded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing.medium),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(radius.card),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(spacing.medium)) {
                        Text(
                            text = "Monitoring Disturbance Detected",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Repeated unexpected app stops identified. Please complete setup checks to restore continuous safe browsing protections.",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                            Button(
                                onClick = { navController.navigate("permission") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(radius.small)
                            ) {
                                Text("Repair Setup", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.resetResilienceCrashes() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(radius.small)
                            ) {
                                Text("Ignore", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Encouragement Greeting Banner (Recovery Overview)
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.medium),
                cornerRadius = radius.card
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(
                        text = "Recovery Mindset",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = encouragementMessage,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Circular Risk Indicator Card
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.medium),
                cornerRadius = radius.outer
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Risk State",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale) // Breathing animation
                    ) {
                        // Ambient radial glow
                        val riskColor = when (riskState.level) {
                            RiskLevel.LOW -> MaterialTheme.colorScheme.primary
                            RiskLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
                            RiskLevel.HIGH -> MaterialTheme.colorScheme.tertiary
                            RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                        }
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(riskColor.copy(alpha = 0.18f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )

                        // Scoring circle
                        Box(
                            modifier = Modifier
                                .size(114.dp)
                                .background(MaterialTheme.colorScheme.background, CircleShape)
                                .border(2.dp, riskColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${riskState.score}",
                                    color = Color.White,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "SCORE",
                                    color = riskColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "LEVEL: ${riskState.level.name}",
                        color = when (riskState.level) {
                            RiskLevel.LOW -> MaterialTheme.colorScheme.primary
                            RiskLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
                            RiskLevel.HIGH -> MaterialTheme.colorScheme.tertiary
                            RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Monitored App Status row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.medium)
                    .clip(RoundedCornerShape(radius.medium))
                    .background(GlassCardBackgroundBrush)
                    .border(1.dp, GlassBorderBrush, RoundedCornerShape(radius.medium))
                    .padding(spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(spacing.small))
                Text(
                    text = "Monitored Active App: ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = activeApp.ifEmpty { "None" }.substringAfterLast("."),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Local DNS VPN status panel
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.medium),
                cornerRadius = radius.card
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isVpnActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(spacing.small))
                            Text(
                                text = "DNS Safe Browsing Filter",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isVpnActive) "Protected" else "Inactive",
                            color = if (isVpnActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Filtered attempts today:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("$blockedAttemptsToday", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (cooldownActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(radius.small))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                                .padding(spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cooldown Lock Active", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("${cooldownTimeRemaining / 1000}s", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // System Security & Settings Dashboard Card
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.large),
                cornerRadius = radius.card
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Security & Protection Status",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val systemHealth by viewModel.systemHealthPercentage.collectAsState()
                        Text(
                            text = "$systemHealth% Health",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Local Data Storage:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("Room SQLCipher Secure", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Integrity Check:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(
                            text = if (integrityState.systemIntact) "Secure / Intact" else "Setup Required",
                            color = if (integrityState.systemIntact) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!integrityState.systemIntact) {
                            Button(
                                onClick = { navController.navigate("permission") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(radius.small)
                            ) {
                                Text("Repair Setup", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { navController.navigate("settings") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(radius.small)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("App Settings", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(90.dp))
        }

        FloatingBottomNavigation(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
}
