package dev.wenyu.semanticcontrol.app

enum class HomepageSetupStatus {
    NotEnabled,
    Enabled,
    NeedsAttention,
}

enum class HomepageCtaAction {
    OpenAccessibilitySettings,
}

data class HomepageSetupCardState(
    val status: HomepageSetupStatus,
    val statusLineResId: Int,
    val helperLineResId: Int,
    val ctaLabelResId: Int,
    val ctaAction: HomepageCtaAction,
)

class HomepageSetupCardStateResolver {

    fun resolve(
        isAccessibilityServiceEnabled: Boolean,
        isAccessibilityServiceConnected: Boolean,
    ): HomepageSetupCardState {
        return when {
            !isAccessibilityServiceEnabled -> HomepageSetupCardState(
                status = HomepageSetupStatus.NotEnabled,
                statusLineResId = R.string.homepage_status_not_enabled,
                helperLineResId = R.string.homepage_helper_not_enabled,
                ctaLabelResId = R.string.homepage_cta_enable_service,
                ctaAction = HomepageCtaAction.OpenAccessibilitySettings,
            )

            isAccessibilityServiceConnected -> HomepageSetupCardState(
                status = HomepageSetupStatus.Enabled,
                statusLineResId = R.string.homepage_status_enabled,
                helperLineResId = R.string.homepage_helper_enabled,
                ctaLabelResId = R.string.homepage_cta_review_service,
                ctaAction = HomepageCtaAction.OpenAccessibilitySettings,
            )

            else -> HomepageSetupCardState(
                status = HomepageSetupStatus.NeedsAttention,
                statusLineResId = R.string.homepage_status_needs_attention,
                helperLineResId = R.string.homepage_helper_needs_attention,
                ctaLabelResId = R.string.homepage_cta_check_service,
                ctaAction = HomepageCtaAction.OpenAccessibilitySettings,
            )
        }
    }
}
