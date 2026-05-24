package com.behavioralfirewall.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.behavioralfirewall.presentation.screens.*
import com.behavioralfirewall.presentation.screens.escalation.IntentEscalationDashboardScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("onboarding") { OnboardingScreen(navController) }
        composable("permission") { PermissionSetupScreen(navController) }
        composable("home") { HomeDashboardScreen(navController) }
        composable("statistics") { StatisticsScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("overlay") { InterventionOverlayScreen(navController) }
        composable("accountability") { com.behavioralfirewall.presentation.screens.accountability.AccountabilityDashboardScreen(navController) }
        composable("privacy_controls") { com.behavioralfirewall.presentation.screens.accountability.PrivacyControlsScreen(navController) }
        composable("blocked_page") { BlockedPageScreen(navController) }
        composable("diagnostics") { DiagnosticsScreen(viewModel = androidx.hilt.navigation.compose.hiltViewModel(), onBack = { navController.popBackStack() }, onNavigateToEscalationDashboard = { navController.navigate("escalation_dashboard") }) }
        composable("privacy_settings") { PrivacySettingsScreen(viewModel = androidx.hilt.navigation.compose.hiltViewModel(), onBack = { navController.popBackStack() }) }
        composable("escalation_dashboard") { IntentEscalationDashboardScreen(viewModel = androidx.hilt.navigation.compose.hiltViewModel(), onBack = { navController.popBackStack() }) }
    }
}
