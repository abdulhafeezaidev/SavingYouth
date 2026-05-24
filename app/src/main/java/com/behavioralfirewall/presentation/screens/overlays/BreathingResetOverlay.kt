package com.behavioralfirewall.presentation.screens.overlays

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.behavioralfirewall.presentation.theme.PremiumBackgroundGradient
import kotlinx.coroutines.delay

@Composable
fun BreathingResetOverlay(
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var stepText by remember { mutableStateOf("Get ready...") }
    var targetScale by remember { mutableStateOf(1.0f) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var phaseTimer by remember { mutableStateOf(0) }
    
    val maxSeconds = 64 // 4 complete box breathing cycles of 16 seconds each
    val scaleAnim by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 4000), // Smooth 4s transitions
        label = "breathing_scale"
    )

    LaunchedEffect(key1 = Unit) {
        while (elapsedSeconds < maxSeconds) {
            val cycleTime = elapsedSeconds % 16
            when (cycleTime) {
                0 -> {
                    stepText = "Inhale slowly..."
                    targetScale = 2.2f
                    phaseTimer = 4
                }
                4 -> {
                    stepText = "Hold the breath..."
                    targetScale = 2.2f
                    phaseTimer = 4
                }
                8 -> {
                    stepText = "Exhale gently..."
                    targetScale = 1.0f
                    phaseTimer = 4
                }
                12 -> {
                    stepText = "Hold empty..."
                    targetScale = 1.0f
                    phaseTimer = 4
                }
            }
            
            delay(1000L)
            elapsedSeconds++
            phaseTimer--
        }
        onComplete()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumBackgroundGradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Box Breathing Reset",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Regulate your nervous system. Inhale 4s, Hold 4s, Exhale 4s, Hold 4s.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Guided breathing bubble with animated scale
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    // Outer ring
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                    )
                    
                    // Breathing bubble
                    Box(
                        modifier = Modifier
                            .size((90 * scaleAnim).dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                                    )
                                ),
                                CircleShape
                            )
                    )

                    // Text status inside the circle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stepText,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${phaseTimer}s",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Global progress indicator
                Text(
                    text = "Reset Progress: ${elapsedSeconds * 100 / maxSeconds}%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LinearProgressIndicator(
                    progress = { elapsedSeconds.toFloat() / maxSeconds.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Skip exercise", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
