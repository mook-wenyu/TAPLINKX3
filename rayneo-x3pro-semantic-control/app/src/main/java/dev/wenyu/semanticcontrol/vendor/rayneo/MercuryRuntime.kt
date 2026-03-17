package dev.wenyu.semanticcontrol.vendor.rayneo

import android.app.Application
import com.ffalcon.mercury.android.sdk.MercurySDK

interface MercuryRuntime {
    fun isAvailable(): Boolean

    fun init(application: Application)
}

class MercurySdkRuntime : MercuryRuntime {
    override fun isAvailable(): Boolean = true

    override fun init(application: Application) {
        MercurySDK.init(application)
    }
}
