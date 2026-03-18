package dev.wenyu.semanticcontrol.app

import org.junit.Assert.assertEquals
import org.junit.Test

class HomepageSetupCardStateTest {

    private val resolver = HomepageSetupCardStateResolver()

    @Test
    fun `resolve returns native only when preferred mode is native and service is off`() {
        val state = resolver.resolve(
            preferredMode = AccessibilityMode.NativeOnly,
            isAccessibilityServiceEnabled = false,
            isAccessibilityServiceConnected = false,
        )

        assertEquals(AccessibilityMode.NativeOnly, state.resolvedMode)
        assertEquals(HomepageSetupStatus.NativeOnly, state.status)
        assertEquals(HomepageCtaAction.OpenAccessibilitySettings, state.ctaAction)
        assertEquals(R.string.homepage_cta_enable_enhancement, state.ctaLabelResId)
        assertEquals(R.string.homepage_status_native_only, state.statusLineResId)
        assertEquals(R.string.homepage_helper_native_only, state.helperLineResId)
        assertEquals(AccessibilityMode.AccessibilityEnhanced, state.preferredModeOnPrimaryAction)
    }

    @Test
    fun `resolve upgrades to enhancement mode when service is on and connected`() {
        val state = resolver.resolve(
            preferredMode = AccessibilityMode.NativeOnly,
            isAccessibilityServiceEnabled = true,
            isAccessibilityServiceConnected = true,
        )

        assertEquals(AccessibilityMode.AccessibilityEnhanced, state.resolvedMode)
        assertEquals(HomepageSetupStatus.EnhancementActive, state.status)
        assertEquals(HomepageCtaAction.OpenAccessibilitySettings, state.ctaAction)
        assertEquals(R.string.homepage_cta_review_service, state.ctaLabelResId)
        assertEquals(R.string.homepage_status_enhancement_active, state.statusLineResId)
        assertEquals(R.string.homepage_helper_enhancement_active, state.helperLineResId)
    }

    @Test
    fun `resolve returns needs attention when enhancement mode was preferred but service is off`() {
        val state = resolver.resolve(
            preferredMode = AccessibilityMode.AccessibilityEnhanced,
            isAccessibilityServiceEnabled = false,
            isAccessibilityServiceConnected = false,
        )

        assertEquals(AccessibilityMode.AccessibilityEnhanced, state.resolvedMode)
        assertEquals(HomepageSetupStatus.NeedsAttention, state.status)
        assertEquals(HomepageCtaAction.OpenAccessibilitySettings, state.ctaAction)
        assertEquals(R.string.homepage_cta_check_service, state.ctaLabelResId)
        assertEquals(R.string.homepage_status_needs_attention, state.statusLineResId)
        assertEquals(R.string.homepage_helper_needs_attention, state.helperLineResId)
    }

    @Test
    fun `resolve returns needs attention when accessibility service is enabled but disconnected`() {
        val state = resolver.resolve(
            preferredMode = AccessibilityMode.AccessibilityEnhanced,
            isAccessibilityServiceEnabled = true,
            isAccessibilityServiceConnected = false,
        )

        assertEquals(AccessibilityMode.AccessibilityEnhanced, state.resolvedMode)
        assertEquals(HomepageSetupStatus.NeedsAttention, state.status)
        assertEquals(HomepageCtaAction.OpenAccessibilitySettings, state.ctaAction)
        assertEquals(R.string.homepage_cta_check_service, state.ctaLabelResId)
        assertEquals(R.string.homepage_status_needs_attention, state.statusLineResId)
        assertEquals(R.string.homepage_helper_needs_attention, state.helperLineResId)
    }
}
