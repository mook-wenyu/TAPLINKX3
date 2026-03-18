package dev.wenyu.semanticcontrol.core.contracts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSupportTierTest {

    @Test
    fun `all good signals map to generic support`() {
        val audit = AppAuditSnapshot(
            accessibilityTree = AuditQuality.Good,
            focusability = AuditQuality.Good,
            activation = AuditQuality.Good,
        )

        assertEquals(AppSupportTier.Generic, audit.supportTier())
        assertFalse(audit.shouldEvaluateFocusTracker())
    }

    @Test
    fun `partial focusability maps to whitelist candidate and suggests focus tracker evaluation`() {
        val audit = AppAuditSnapshot(
            accessibilityTree = AuditQuality.Good,
            focusability = AuditQuality.Partial,
            activation = AuditQuality.Good,
        )

        assertEquals(AppSupportTier.WhitelistCandidate, audit.supportTier())
        assertTrue(audit.shouldEvaluateFocusTracker())
    }

    @Test
    fun `partial tree quality can still be a whitelist candidate without focus tracker experiment`() {
        val audit = AppAuditSnapshot(
            accessibilityTree = AuditQuality.Partial,
            focusability = AuditQuality.Good,
            activation = AuditQuality.Good,
        )

        assertEquals(AppSupportTier.WhitelistCandidate, audit.supportTier())
        assertFalse(audit.shouldEvaluateFocusTracker())
    }

    @Test
    fun `poor tree quality maps to unsupported`() {
        val audit = AppAuditSnapshot(
            accessibilityTree = AuditQuality.Poor,
            focusability = AuditQuality.Good,
            activation = AuditQuality.Good,
        )

        assertEquals(AppSupportTier.Unsupported, audit.supportTier())
        assertFalse(audit.shouldEvaluateFocusTracker())
    }

    @Test
    fun `poor activation maps to unsupported`() {
        val audit = AppAuditSnapshot(
            accessibilityTree = AuditQuality.Good,
            focusability = AuditQuality.Partial,
            activation = AuditQuality.Poor,
        )

        assertEquals(AppSupportTier.Unsupported, audit.supportTier())
        assertFalse(audit.shouldEvaluateFocusTracker())
    }
}
