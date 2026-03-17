package dev.wenyu.semanticcontrol.app

import android.app.Application
import android.util.Log
import dev.wenyu.semanticcontrol.vendor.rayneo.BootstrapResult
import dev.wenyu.semanticcontrol.vendor.rayneo.RayNeoVendorAdapter

class SemanticControlApplication : Application() {

    private val rayNeoVendorAdapter = RayNeoVendorAdapter()

    override fun onCreate() {
        super.onCreate()
        val bootstrapResult = rayNeoVendorAdapter.bootstrap(this)
        Log.i(TAG, "Semantic control application created: $bootstrapResult")
    }

    private companion object {
        const val TAG = "SemanticControlApp"
    }
}
