package dev.wenyu.semanticcontrol.app

import android.content.SharedPreferences

interface AccessibilityModeStore {
    fun read(): AccessibilityMode

    fun write(mode: AccessibilityMode)
}

class SharedPreferencesAccessibilityModeStore(
    private val preferences: SharedPreferences,
) : AccessibilityModeStore {

    override fun read(): AccessibilityMode {
        return when (preferences.getString(KEY_MODE, null)) {
            AccessibilityMode.AccessibilityEnhanced.name -> AccessibilityMode.AccessibilityEnhanced
            else -> AccessibilityMode.NativeOnly
        }
    }

    override fun write(mode: AccessibilityMode) {
        preferences.edit().putString(KEY_MODE, mode.name).apply()
    }

    companion object {
        const val PREFS_NAME = "accessibility_mode"
        private const val KEY_MODE = "mode"
    }
}
