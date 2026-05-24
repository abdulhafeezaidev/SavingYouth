package com.behavioralfirewall.services.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.behavioralfirewall.domain.engine.InterventionManager
import com.behavioralfirewall.domain.engine.ReplacementActionManager
import com.behavioralfirewall.domain.engine.MemorySafetyManager
import com.behavioralfirewall.domain.model.InterventionType
import com.behavioralfirewall.presentation.screens.overlays.CountdownDelayOverlay
import com.behavioralfirewall.presentation.screens.overlays.ReflectionPromptOverlay
import com.behavioralfirewall.presentation.screens.overlays.BreathingResetOverlay
import com.behavioralfirewall.presentation.screens.overlays.RedirectActionOverlay
import com.behavioralfirewall.presentation.theme.BehavioralFirewallTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OverlayInterventionService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject
    lateinit var interventionManager: InterventionManager

    @Inject
    lateinit var replacementActionManager: ReplacementActionManager

    @Inject
    lateinit var memorySafetyManager: MemorySafetyManager

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null

    // Custom LifecycleOwner implementation for drawing Compose Views outside an Activity
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    // Custom ViewModelStoreOwner implementation
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    // Custom SavedStateRegistryOwner implementation
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Collect the live intervention StateFlow
        serviceScope.launch {
            interventionManager.activeIntervention.collect { interventionType ->
                if (interventionType != null) {
                    showOverlay(interventionType)
                } else {
                    hideOverlay()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return START_NOT_STICKY
    }

    private fun showOverlay(type: InterventionType) {
        if (composeView != null) return // Already showing

        val context = this
        val newView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(context)
            setViewTreeViewModelStoreOwner(context)
            setViewTreeSavedStateRegistryOwner(context)
            
            setContent {
                BehavioralFirewallTheme {
                    when (type) {
                        InterventionType.WARNING -> {
                            ReflectionPromptOverlay(
                                onDismiss = { interventionManager.dismissIntervention() },
                                onComply = { interventionManager.complyIntervention() }
                            )
                        }
                        InterventionType.DELAY -> {
                            CountdownDelayOverlay(
                                durationSeconds = 15,
                                onComplete = { interventionManager.complyIntervention() },
                                onDismiss = { interventionManager.dismissIntervention() }
                            )
                        }
                        InterventionType.BREATHING -> {
                            BreathingResetOverlay(
                                onComplete = { interventionManager.complyIntervention() },
                                onDismiss = { interventionManager.dismissIntervention() }
                            )
                        }
                        InterventionType.REFLECTION_PROMPT -> {
                            ReflectionPromptOverlay(
                                onDismiss = { interventionManager.dismissIntervention() },
                                onComply = { interventionManager.complyIntervention() }
                            )
                        }
                        InterventionType.TASK_REDIRECT -> {
                            RedirectActionOverlay(
                                onDismiss = { interventionManager.dismissIntervention() },
                                onComply = { interventionManager.complyIntervention() }
                            )
                        }
                        InterventionType.HARD_INTERRUPT -> {
                            CountdownDelayOverlay(
                                durationSeconds = 30,
                                onComplete = { interventionManager.complyIntervention() },
                                onDismiss = { interventionManager.dismissIntervention() }
                            )
                        }
                    }
                }
            }
        }

        val layoutParams = WindowManager.LayoutParams().apply {
            this.type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }

        try {
            windowManager?.addView(newView, layoutParams)
            composeView = newView
            memorySafetyManager.registerOverlay(newView, this)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideOverlay() {
        composeView?.let { view ->
            try {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                windowManager?.removeView(view)
                memorySafetyManager.unregisterOverlay(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            composeView = null
        }
    }

    override fun onDestroy() {
        hideOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceJob.cancel()
        super.onDestroy()
    }
}
