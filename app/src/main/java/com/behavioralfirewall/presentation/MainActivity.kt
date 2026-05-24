package com.behavioralfirewall.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.behavioralfirewall.domain.release.FinalPerformanceValidator
import com.behavioralfirewall.presentation.navigation.AppNavigation
import com.behavioralfirewall.presentation.theme.BehavioralFirewallTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var performanceValidator: FinalPerformanceValidator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performanceValidator.recordAppLaunchStart()
        
        setContent {
            BehavioralFirewallTheme {
                AppNavigation()
            }
        }
        
        performanceValidator.recordAppLaunchComplete()
    }
}
