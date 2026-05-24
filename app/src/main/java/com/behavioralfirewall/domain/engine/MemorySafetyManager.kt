package com.behavioralfirewall.domain.engine

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemorySafetyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keep track of active window overlays using WeakReferences to avoid leaks
    private val activeOverlays = mutableListOf<WeakReference<View>>()
    
    private val _trackedOverlaysCount = MutableStateFlow(0)
    val trackedOverlaysCount = _trackedOverlaysCount.asStateFlow()

    private val _systemMemoryState = MutableStateFlow("Normal")
    val systemMemoryState = _systemMemoryState.asStateFlow()

    /**
     * Registers an overlay view to be monitored for lifecycle safety.
     * Attach a lifecycle observer to auto-remove when destroyed.
     */
    fun registerOverlay(view: View, lifecycleOwner: LifecycleOwner) {
        val weakView = WeakReference(view)
        activeOverlays.add(weakView)
        _trackedOverlaysCount.value = activeOverlays.filter { it.get() != null }.size
        
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    unregisterOverlay(view)
                    lifecycleOwner.lifecycle.removeObserver(this)
                }
            }
        })
    }

    /**
     * Explicitly unregisters an overlay view, cleaning up references.
     */
    fun unregisterOverlay(view: View) {
        val iterator = activeOverlays.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next().get()
            if (ref == null || ref == view) {
                iterator.remove()
            }
        }
        _trackedOverlaysCount.value = activeOverlays.filter { it.get() != null }.size
    }

    /**
     * Clean up all stale or leaked views.
     */
    fun performEmergencyMemoryCleanup() {
        val iterator = activeOverlays.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next().get()
            if (ref == null) {
                iterator.remove()
            } else {
                if (!ref.isAttachedToWindow) {
                    iterator.remove()
                }
            }
        }
        _trackedOverlaysCount.value = activeOverlays.size
        System.gc() // Suggest garbage collection to clean up stale resources
    }

    fun updateMemoryState(level: Int) {
        _systemMemoryState.value = when (level) {
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "Critical Low"
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "Running Low"
            android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "Background Trim"
            else -> "Normal"
        }
    }
}
