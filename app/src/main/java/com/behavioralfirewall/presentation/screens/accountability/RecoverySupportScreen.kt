package com.behavioralfirewall.presentation.screens.accountability

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun RecoverySupportScreen(
    onComplete: (trigger: String, emotion: String, plan: String) -> Unit
) {
    var stepIndex by remember { mutableStateOf(0) }
    var selectedTrigger by remember { mutableStateOf("") }
    var selectedEmotion by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("") }

    val questions = listOf(
        "What triggered the urge?",
        "What emotional state preceded this?",
        "How can we disrupt this sequence next time?"
    )

    val options = listOf(
        listOf("Fatigue/Sleepiness", "Stress/Overwhelm", "Boredom/Downtime", "Habitual Swiping"),
        listOf("Loneliness", "Anxiety/Restlessness", "Mental exhaustion", "Numbness/Dullness"),
        listOf("Add a 30s box breathing break", "Move to a public space immediately", "Trigger Emergency support overlay", "Call partner/mentor")
    )

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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Guided Growth Reflection",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A setback is just data. It tells us where the fence has a gap. Let's analyze it and rebuild together.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Step Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    questions.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (idx == stepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                // Main Questionnaire card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = questions[stepIndex],
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Grid of options
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options[stepIndex].forEach { option ->
                        Button(
                            onClick = {
                                when (stepIndex) {
                                    0 -> selectedTrigger = option
                                    1 -> selectedEmotion = option
                                    2 -> selectedPlan = option
                                }

                                if (stepIndex < questions.size - 1) {
                                    stepIndex++
                                } else {
                                    onComplete(selectedTrigger, selectedEmotion, selectedPlan)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        ) {
                            Text(option, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (stepIndex > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { stepIndex-- }) {
                        Text("Back", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}