package com.behavioralfirewall.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.behavioralfirewall.presentation.components.FloatingBottomNavigation
import com.behavioralfirewall.presentation.components.GlassmorphicCard
import com.behavioralfirewall.presentation.theme.GlassBorderBrush
import com.behavioralfirewall.presentation.theme.GlassCardBackgroundBrush
import com.behavioralfirewall.presentation.theme.LocalRadius
import com.behavioralfirewall.presentation.theme.LocalSpacing
import com.behavioralfirewall.presentation.theme.PremiumBackgroundGradient

@Composable
fun SettingsScreen(navController: NavController) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val scrollState = rememberScrollState()

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
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            // Header Bar (Custom TopAppBar matching Figma)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small, bottom = spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(spacing.medium))
                
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Header Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = radius.card
            ) {
                Column {
                    Text(
                        text = "Settings & Diagnostics",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configure your safety triggers, diagnostics logging, and recovery telemetry.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            // 1. Protection Group Card
            SettingsGroupCard(title = "Protection Settings") {
                SettingsItemRow(
                    title = "Verify Permissions Setup",
                    subtitle = "Accessibility service, overlays, and restrictions",
                    icon = Icons.Default.Shield,
                    onClick = { navController.navigate("permission") }
                )
            }

            // 2. Security & Privacy Group Card
            SettingsGroupCard(title = "Security & Privacy") {
                SettingsItemRow(
                    title = "Privacy & Backup Controls",
                    subtitle = "Erase local logs, export JSON, and restore backups",
                    icon = Icons.Default.Lock,
                    onClick = { navController.navigate("privacy_settings") }
                )
            }

            // 3. Diagnostics Group Card
            SettingsGroupCard(title = "System Diagnostics & Telemetry") {
                SettingsItemRow(
                    title = "Developer Diagnostics Panel",
                    subtitle = "Latency logging, memory GC triggers, and health audits",
                    icon = Icons.Default.Security,
                    onClick = { navController.navigate("diagnostics") }
                )
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        FloatingBottomNavigation(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SettingsGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val radius = LocalRadius.current
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(radius.card),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
        ) {
            Column(
                modifier = Modifier.background(brush = GlassCardBackgroundBrush),
                content = content
            )
        }
    }
}

@Composable
fun SettingsItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(spacing.medium))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
