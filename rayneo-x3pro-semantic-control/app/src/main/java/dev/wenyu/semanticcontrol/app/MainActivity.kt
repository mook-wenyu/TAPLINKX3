package dev.wenyu.semanticcontrol.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import dev.wenyu.semanticcontrol.feature.semantic.SemanticAccessibilityService

class MainActivity : AppCompatActivity() {

    private val stateResolver = HomepageSetupCardStateResolver()

    private lateinit var titleText: TextView
    private lateinit var statusText: TextView
    private lateinit var helperText: TextView
    private lateinit var primaryButton: MaterialButton

    private var currentCtaAction = HomepageCtaAction.OpenAccessibilitySettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        titleText = findViewById(R.id.titleText)
        statusText = findViewById(R.id.statusText)
        helperText = findViewById(R.id.helperText)
        primaryButton = findViewById(R.id.primaryButton)
        primaryButton.setOnClickListener {
            handleCta(currentCtaAction)
        }
        renderHomepage()
    }

    override fun onResume() {
        super.onResume()
        renderHomepage()
    }

    private fun renderHomepage() {
        val state = stateResolver.resolve(
            isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(),
            isAccessibilityServiceConnected = SemanticAccessibilityService.activeInstance != null,
        )

        titleText.setText(R.string.homepage_title)
        statusText.setText(state.statusLineResId)
        helperText.setText(state.helperLineResId)
        primaryButton.setText(state.ctaLabelResId)
        currentCtaAction = state.ctaAction
    }

    private fun handleCta(action: HomepageCtaAction) {
        if (action == HomepageCtaAction.OpenAccessibilitySettings) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        val targetService = ComponentName(this, SemanticAccessibilityService::class.java).flattenToString()

        return enabledServices.split(':').any { it == targetService }
    }
}
