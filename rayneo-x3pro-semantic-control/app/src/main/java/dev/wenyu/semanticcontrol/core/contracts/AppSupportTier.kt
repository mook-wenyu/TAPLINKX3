package dev.wenyu.semanticcontrol.core.contracts

enum class AuditQuality {
    Good,
    Partial,
    Poor,
}

enum class AppSupportTier {
    Generic,
    WhitelistCandidate,
    Unsupported,
}

data class AppAuditSnapshot(
    val accessibilityTree: AuditQuality,
    val focusability: AuditQuality,
    val activation: AuditQuality,
) {
    fun supportTier(): AppSupportTier {
        if (accessibilityTree == AuditQuality.Poor || activation == AuditQuality.Poor) {
            return AppSupportTier.Unsupported
        }

        if (
            accessibilityTree == AuditQuality.Good &&
            focusability == AuditQuality.Good &&
            activation == AuditQuality.Good
        ) {
            return AppSupportTier.Generic
        }

        return if (focusability == AuditQuality.Poor) {
            AppSupportTier.Unsupported
        } else {
            AppSupportTier.WhitelistCandidate
        }
    }

    fun shouldEvaluateFocusTracker(): Boolean {
        return supportTier() == AppSupportTier.WhitelistCandidate &&
            accessibilityTree == AuditQuality.Good &&
            focusability == AuditQuality.Partial &&
            activation != AuditQuality.Poor
    }
}
