package com.behavioralfirewall.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.behavioralfirewall.presentation.theme.PremiumBackgroundGradient
import com.behavioralfirewall.presentation.viewmodel.PrivacySettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    viewModel: PrivacySettingsViewModel,
    onBack: () -> Unit
) {
    val cloudSyncEnabled by viewModel.cloudSyncEnabled.collectAsState()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState()
    val discreetNotifications by viewModel.discreetNotifications.collectAsState()
    val exportPayload by viewModel.exportPayload.collectAsState()
    val backupPayload by viewModel.backupPayload.collectAsState()
    val actionStatus by viewModel.actionStatus.collectAsState()

    var backupInputText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionStatus) {
        if (actionStatus.isNotEmpty()) {
            snackbarHostState.showSnackbar(actionStatus)
            viewModel.clearActionStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Safe Data", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PremiumBackgroundGradient)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro text protecting dignity
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Trust & Dignity Matters",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All behavioral records are stored locally and encrypted using AES-256 GCM backed by the Android Keystore. We prioritize transparency, privacy, and user dignity.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Privacy toggles
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Privacy Controls",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            PrivacySwitchRow(
                                title = "Discreet Notification Content",
                                subtitle = "Uses neutral wellness phrasing for all alerts and lockscreens",
                                checked = discreetNotifications,
                                onCheckedChange = { viewModel.toggleDiscreetNotifications(it) }
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

                            PrivacySwitchRow(
                                title = "Cloud Storage Synchronization",
                                subtitle = "Backs up anonymized settings metadata to Appwrite cloud",
                                checked = cloudSyncEnabled,
                                onCheckedChange = { viewModel.toggleCloudSync(it) }
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

                            PrivacySwitchRow(
                                title = "Analytics Integration",
                                subtitle = "Gathers general app stability diagnostic reports",
                                checked = analyticsEnabled,
                                onCheckedChange = { viewModel.toggleAnalytics(it) }
                            )
                        }
                    }
                }

                // Data export and erasure
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Erase & Export Logs",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Erase all local risk event logs and domain block history. Your active recovery streaks are preserved to protect your milestones.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.exportLocalData() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Text("Export Data", color = MaterialTheme.colorScheme.primary)
                                }

                                Button(
                                    onClick = { viewModel.clearBehavioralData() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                                ) {
                                    Text("Erase Logs", color = MaterialTheme.colorScheme.error)
                                }
                            }

                            if (exportPayload.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = exportPayload,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Exported JSON Payload") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.LightGray)
                                )
                            }
                        }
                    }
                }

                // Encrypted backup
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Encrypted Local Backups",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Generate or restore local encrypted backup payloads to transfer streaks and settings securely between installs.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.createSecureBackup() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Text("Create Backup", color = MaterialTheme.colorScheme.primary)
                                }

                                Button(
                                    onClick = { viewModel.restoreSecureBackup(backupInputText) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Restore Payload", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }

                            if (backupPayload.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = backupPayload,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Encrypted Backup String (Copy me)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.LightGray)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = backupInputText,
                                onValueChange = { backupInputText = it },
                                label = { Text("Paste Encrypted String to Restore") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
