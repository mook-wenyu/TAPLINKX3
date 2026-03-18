package dev.wenyu.semanticcontrol.app

import org.junit.Assert.assertEquals
import org.junit.Test

class HomepageSetupCardStateTest {

    private val resolver = HomepageSetupCardStateResolver()

    @Test
    fun `resolve returns not enabled when accessibility service is off`() {
        val state = resolver.resolve(
            isAccessibilityServiceEnabled = false,
            isAccessibilityServiceConnected = false,
        )

        assertEquals(HomepageSetupStatus.NotEnabled, state.status)
        assertEquals(HomepageCtaAction.OpenAccessibilitySettings, state.ctaAction)
        assertEquals(R.string.homepage_cta_enable_service, state.ctaLabelResId)
        assertEquals(R.string.homepage_status_not_enabled, state.statusLineResId)
        assertEquals(R.string.homepage_helper_not_enabled, state.helperLineResId)
    }

    @Test
    fun `resolve returns enabled when accessibility service is on and connected`() {
        val state = resolver.resolve(
            isAccessibilityServiceEnabled = true,
            isAccessibilityServiceConnected = true,
        )

        assertEquals(HomepageSetupStatus.Enabled, state.status)
        assertEquals(HomepageCtaAction.OpenAccessibilitySettings, state.ctaAction)
        assertEquals(R.string.homepage_cta_review_service, state.ctaLabelResId)
        assertEquals(R.string.homepage_status_enabled, state.statusLineResId)
        assertEquals(R.string.homepage_helper_enabled, state.helperLineResId)
    }

    @Test
    fun `resolve returns needs attention when accessibility service is enabled but disconnected`() {
        val state = resolver.resolve(
            isAccessibilityServiceEnabled = true,
            isAccessibilityServiceConnected = false,
        )

        assertEquals(HomepageSetupStatus.NeedsAttention, state.status)
        assertEquals(HomepageCtaAction.OpenAccessibilitySettings, state.ctaAction)
        assertEquals(R.string.homepage_cta_check_service, state.ctaLabelResId)
        assertEquals(R.string.homepage_status_needs_attention, state.statusLineResId)
        assertEquals(R.string.homepage_helper_needs_attention, state.helperLineResId)
    }
}
