package dev.wenyu.semanticcontrol.vendor.rayneo

import android.app.Application
import dev.wenyu.semanticcontrol.core.contracts.SemanticAction

class RayNeoVendorAdapter(
    private val mercuryRuntime: MercuryRuntime = MercurySdkRuntime(),
) {

    fun bootstrap(application: Application): BootstrapResult {
        if (!mercuryRuntime.isAvailable()) {
            return BootstrapResult.Skipped
        }

        return runCatching {
                mercuryRuntime.init(application)
                BootstrapResult.Initialized
            }
            .getOrElse { error ->
                BootstrapResult.Failed(
                    reason = "${error::class.java.simpleName}: ${error.message ?: "unknown error"}",
                )
            }
    }

    fun createInputAdapter(
        onSemanticAction: (SemanticAction) -> Boolean,
    ): VendorInputAdapter {
        return MercuryTouchInputAdapter(onSemanticAction)
    }
}
