package dev.wenyu.semanticcontrol.app

enum class AccessibilityMode {
    NativeOnly,
    AccessibilityEnhanced,
}

enum class HomepageSetupStatus {
    NativeOnly,
    EnhancementActive,
    NeedsAttention,
}

enum class HomepageCtaAction {
    OpenAccessibilitySettings,
}

data class HomepageSetupCardState(
    val resolvedMode: AccessibilityMode,
    val status: HomepageSetupStatus,
    val statusLineResId: Int,
    val helperLineResId: Int,
    val ctaLabelResId: Int,
    val ctaAction: HomepageCtaAction,
    val preferredModeOnPrimaryAction: AccessibilityMode? = null,
)

class HomepageSetupCardStateResolver {

    fun resolve(
        preferredMode: AccessibilityMode,
        isAccessibilityServiceEnabled: Boolean,
        isAccessibilityServiceConnected: Boolean,
    ): HomepageSetupCardState {
        return when {
            isAccessibilityServiceEnabled && isAccessibilityServiceConnected -> HomepageSetupCardState(
                resolvedMode = AccessibilityMode.AccessibilityEnhanced,
                status = HomepageSetupStatus.EnhancementActive,
                statusLineResId = R.string.homepage_status_enhancement_active,
                helperLineResId = R.string.homepage_helper_enhancement_active,
                ctaLabelResId = R.string.homepage_cta_review_service,
                ctaAction = HomepageCtaAction.OpenAccessibilitySettings,
            )

            preferredMode == AccessibilityMode.AccessibilityEnhanced -> HomepageSetupCardState(
                resolvedMode = AccessibilityMode.AccessibilityEnhanced,
                status = HomepageSetupStatus.NeedsAttention,
                statusLineResId = R.string.homepage_status_needs_attention,
                helperLineResId = R.string.homepage_helper_needs_attention,
                ctaLabelResId = R.string.homepage_cta_check_service,
                ctaAction = HomepageCtaAction.OpenAccessibilitySettings,
            )

            else -> HomepageSetupCardState(
                resolvedMode = AccessibilityMode.NativeOnly,
                status = HomepageSetupStatus.NativeOnly,
                statusLineResId = R.string.homepage_status_native_only,
                helperLineResId = R.string.homepage_helper_native_only,
                ctaLabelResId = R.string.homepage_cta_enable_enhancement,
                ctaAction = HomepageCtaAction.OpenAccessibilitySettings,
                preferredModeOnPrimaryAction = AccessibilityMode.AccessibilityEnhanced,
            )
        }
    }
}
