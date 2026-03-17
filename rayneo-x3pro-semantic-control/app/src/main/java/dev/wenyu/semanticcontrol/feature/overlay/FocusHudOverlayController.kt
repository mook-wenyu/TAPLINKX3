package dev.wenyu.semanticcontrol.feature.overlay

import android.content.Context
import android.util.Log

class FocusHudOverlayController(
    private val context: Context,
) {
    fun attach(state: FocusHudState) {
        Log.i(TAG, "Attach HUD overlay: $state for ${context.packageName}")
    }

    fun update(state: FocusHudState) {
        Log.d(TAG, "Update HUD overlay: $state")
    }

    fun detach() {
        Log.i(TAG, "Detach HUD overlay")
    }

    private companion object {
        const val TAG = "FocusHudOverlay"
    }
}
