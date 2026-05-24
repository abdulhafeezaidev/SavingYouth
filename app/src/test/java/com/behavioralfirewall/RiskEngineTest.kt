package com.behavioralfirewall

import com.behavioralfirewall.domain.engine.AdaptiveRiskManager
import com.behavioralfirewall.domain.engine.BrowserDetector
import com.behavioralfirewall.domain.engine.RiskEngineImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class RiskEngineTest {

    private lateinit var mockBrowserDetector: BrowserDetector
    private lateinit var mockAdaptiveRiskManager: AdaptiveRiskManager
    private lateinit var riskEngine: RiskEngineImpl

    @Before
    fun setUp() {
        mockBrowserDetector = mock(BrowserDetector::class.java)
        mockAdaptiveRiskManager = mock(AdaptiveRiskManager::class.java)
        
        // Setup default mocks
        `when`(mockAdaptiveRiskManager.lateNightMultiplier).thenReturn(1.0f)
        `when`(mockAdaptiveRiskManager.playStoreMultiplier).thenReturn(1.0f)
        `when`(mockAdaptiveRiskManager.switchingMultiplier).thenReturn(1.0f)

        riskEngine = RiskEngineImpl(mockBrowserDetector, mockAdaptiveRiskManager)
    }

    @Test
    fun testNormalDaytimeBrowserSwitch_returnsLowScore() {
        `when`(mockBrowserDetector.isBrowser("com.android.chrome")).thenReturn(true)
        
        // Low duration switch during daytime
        val score = riskEngine.calculateRiskScore(
            packageName = "com.android.chrome",
            sessionDurationMs = 10000L,
            switchCount = 1,
            stressLevel = 30
        )
        
        // Since it's daytime and short duration, risk is low
        assertTrue(score < 50)
    }

    @Test
    fun testLongBrowserSession_escalatesScore() {
        `when`(mockBrowserDetector.isBrowser("com.android.chrome")).thenReturn(true)
        
        // Long session duration (exceeds threshold)
        val score = riskEngine.calculateRiskScore(
            packageName = "com.android.chrome",
            sessionDurationMs = 600000L, // 10 minutes
            switchCount = 1,
            stressLevel = 30
        )
        
        assertTrue(score >= 40)
    }

    @Test
    fun testRapidSwitching_escalatesScore() {
        `when`(mockBrowserDetector.isBrowser("com.android.chrome")).thenReturn(false)
        
        val score = riskEngine.calculateRiskScore(
            packageName = "com.whatsapp",
            sessionDurationMs = 5000L,
            switchCount = 8, // More than 5 switches
            stressLevel = 30
        )
        
        assertTrue(score >= 20)
    }

    @Test
    fun testEmptyPackage_returnsZero() {
        val score = riskEngine.calculateRiskScore(
            packageName = "",
            sessionDurationMs = 0L,
            switchCount = 0,
            stressLevel = 0
        )
        
        assertEquals(0, score)
    }
}
