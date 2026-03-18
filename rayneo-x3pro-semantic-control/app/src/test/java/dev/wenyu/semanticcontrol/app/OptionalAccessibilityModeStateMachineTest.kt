package dev.wenyu.semanticcontrol.app

import org.junit.Assert.assertEquals
import org.junit.Test

class OptionalAccessibilityModeStateMachineTest {

    private val stateMachine = HomepageSetupCardStateResolver()

    @Test
    fun `native only stays default when accessibility is off`() {
        val state = stateMachine.resolve(
            preferredMode = AccessibilityMode.NativeOnly,
            isAccessibilityServiceEnabled = false,
            isAccessibilityServiceConnected = false,
        )

        assertEquals(AccessibilityMode.NativeOnly, state.resolvedMode)
        assertEquals(HomepageSetupStatus.NativeOnly, state.status)
        assertEquals(R.string.homepage_status_native_only, state.statusLineResId)
        assertEquals(R.string.homepage_helper_native_only, state.helperLineResId)
        assertEquals(R.string.homepage_cta_enable_enhancement, state.ctaLabelResId)
        assertEquals(AccessibilityMode.AccessibilityEnhanced, state.preferredModeOnPrimaryAction)
    }

    @Test
    fun `connected service upgrades mode to accessibility enhanced`() {
        val state = stateMachine.resolve(
            preferredMode = AccessibilityMode.NativeOnly,
            isAccessibilityServiceEnabled = true,
            isAccessibilityServiceConnected = true,
        )

        assertEquals(AccessibilityMode.AccessibilityEnhanced, state.resolvedMode)
        assertEquals(HomepageSetupStatus.EnhancementActive, state.status)
        assertEquals(R.string.homepage_status_enhancement_active, state.statusLineResId)
        assertEquals(R.string.homepage_helper_enhancement_active, state.helperLineResId)
        assertEquals(R.string.homepage_cta_review_service, state.ctaLabelResId)
    }

    @Test
    fun `preferred enhanced mode with disabled service shows needs attention`() {
        val state = stateMachine.resolve(
            preferredMode = AccessibilityMode.AccessibilityEnhanced,
            isAccessibilityServiceEnabled = false,
            isAccessibilityServiceConnected = false,
        )

        assertEquals(AccessibilityMode.AccessibilityEnhanced, state.resolvedMode)
        assertEquals(HomepageSetupStatus.NeedsAttention, state.status)
        assertEquals(R.string.homepage_status_needs_attention, state.statusLineResId)
        assertEquals(R.string.homepage_helper_needs_attention, state.helperLineResId)
        assertEquals(R.string.homepage_cta_check_service, state.ctaLabelResId)
    }

    @Test
    fun `enabled but disconnected service still needs attention`() {
        val state = stateMachine.resolve(
            preferredMode = AccessibilityMode.AccessibilityEnhanced,
            isAccessibilityServiceEnabled = true,
            isAccessibilityServiceConnected = false,
        )

        assertEquals(AccessibilityMode.AccessibilityEnhanced, state.resolvedMode)
        assertEquals(HomepageSetupStatus.NeedsAttention, state.status)
    }
}
