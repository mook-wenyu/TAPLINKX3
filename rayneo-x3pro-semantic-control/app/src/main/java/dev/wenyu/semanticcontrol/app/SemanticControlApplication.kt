package dev.wenyu.semanticcontrol.app

import android.app.Application
import android.util.Log

class SemanticControlApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Semantic control application created")
    }

    private companion object {
        const val TAG = "SemanticControlApp"
    }
}
