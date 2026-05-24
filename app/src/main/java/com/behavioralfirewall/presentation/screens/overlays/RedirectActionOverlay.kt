package com.behavioralfirewall.presentation.screens.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.behavioralfirewall.domain.model.ReplacementAction
import com.behavioralfirewall.domain.model.ReplacementActionType
import com.behavioralfirewall.presentation.theme.PremiumBackgroundGradient
import kotlinx.coroutines.delay

@Composable
fun RedirectActionOverlay(
    onDismiss: () -> Unit,
    onComply: () -> Unit
) {
    // Hardcoded initial list of replacement actions for UI independence
    val defaultActions = remember {
        listOf(
            ReplacementAction(
                type = ReplacementActionType.BREATHING,
                title = "Calming Breathing Reset",
                description = "Box Breathing (Inhale 4s, Hold 4s, Exhale 4s, Hold 4s)",
                durationSeconds = 60
            ),
            ReplacementAction(
                type = ReplacementActionType.PUSHUPS,
                title = "Re-centering Pushups",
                description = "Perform 10 controlled pushups to re-direct energy physical.",
                durationSeconds = 30
            ),
            ReplacementAction(
                type = ReplacementActionType.HYDRATION,
                title = "Mindful Hydration",
                description = "Drink a full glass of cool water mindfully.",
                durationSeconds = 15
            ),
            ReplacementAction(
                type = ReplacementActionType.WALK,
                title = "Grounding Walk",
                description = "Walk slowly for 5 minutes focusing on steps.",
                durationSeconds = 300
            ),
            ReplacementAction(
                type = ReplacementActionType.STUDY_SPRINT,
                title = "Focus Study Sprint",
                description = "5 minutes of distraction-free studying/focus.",
                durationSeconds = 300
            ),
            ReplacementAction(
                type = ReplacementActionType.JOURNALING,
                title = "Gratitude Journaling",
                description = "Write down 3 things you are grateful for right now.",
                durationSeconds = 45
            )
        )
    }

    var selectedAction by remember { mutableStateOf<ReplacementAction?>(null) }
    var journalText by remember { mutableStateOf("") }
    var activeTimerSeconds by remember { mutableStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = timerRunning, key2 = activeTimerSeconds) {
        if (timerRunning && activeTimerSeconds > 0) {
            delay(1000L)
            activeTimerSeconds--
        } else if (timerRunning && activeTimerSeconds == 0) {
            timerRunning = false
            // Completed!
            onComply()
        }
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
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (selectedAction == null) {
                    // Render Action List Screen
                    Text(
                        text = "Positive Re-direction",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Interrupt the cycle. Choose a replacement action to redirect your energy.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(defaultActions) { action ->
                            ReplacementActionCard(action = action) {
                                selectedAction = action
                                activeTimerSeconds = action.durationSeconds
                                timerRunning = action.durationSeconds > 0 && action.type != ReplacementActionType.HYDRATION && action.type != ReplacementActionType.JOURNALING
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = onDismiss) {
                        Text("Exit and close app", color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                    }
                } else {
                    // Render Specific Action execution screen
                    val action = selectedAction!!
                    
                    Icon(
                        imageVector = getActionIcon(action.type),
                        contentDescription = "Action Icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = action.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = action.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    when (action.type) {
                        ReplacementActionType.HYDRATION -> {
                            Button(
                                onClick = onComply,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("I drank a glass of water", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        ReplacementActionType.JOURNALING -> {
                            OutlinedTextField(
                                value = journalText,
                                onValueChange = { journalText = it },
                                label = { Text("What are you grateful for?") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (journalText.isNotBlank()) onComply()
                                },
                                enabled = journalText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                    disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Submit Reflection", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            // Standard countdown timer display
                            Text(
                                text = formatTime(activeTimerSeconds),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (timerRunning) "Timer Active" else "Timer Paused",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { timerRunning = !timerRunning },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text(if (timerRunning) "Pause" else "Resume", fontSize = 14.sp)
                                }

                                Button(
                                    onClick = onComply,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text("Skip to Complete", fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = { selectedAction = null }) {
                        Text("Back to Actions", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ReplacementActionCard(
    action: ReplacementAction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getActionIcon(action.type),
                    contentDescription = action.title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = action.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

fun getActionIcon(type: ReplacementActionType): ImageVector {
    return when (type) {
        ReplacementActionType.BREATHING -> Icons.Default.SelfImprovement
        ReplacementActionType.PUSHUPS -> Icons.Default.FitnessCenter
        ReplacementActionType.HYDRATION -> Icons.Default.LocalDrink
        ReplacementActionType.WALK -> Icons.Default.DirectionsWalk
        ReplacementActionType.STUDY_SPRINT -> Icons.Default.Timer
        ReplacementActionType.JOURNALING -> Icons.Default.Book
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
