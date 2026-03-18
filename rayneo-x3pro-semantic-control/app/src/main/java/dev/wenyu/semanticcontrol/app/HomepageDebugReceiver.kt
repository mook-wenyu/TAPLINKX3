package dev.wenyu.semanticcontrol.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class HomepageDebugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val activity = MainActivity.activeInstance
        if (activity == null) {
            setResultCode(2)
            setResultData("homepage-unavailable")
            Log.w(TAG, "Received homepage debug command without active homepage")
            return
        }

        val command = HomepageDebugCommand.fromWireValue(intent.getStringExtra(EXTRA_COMMAND))
        if (command == null) {
            setResultCode(3)
            setResultData("unknown-command")
            Log.w(TAG, "Received unknown homepage debug command")
            return
        }

        val result = activity.handleDebugCommand(command)
        setResultCode(if (result.success) 0 else 1)
        setResultData(result.message)
        Log.i(TAG, "Homepage debug command ${command.wireValue} -> ${result.message}")
    }

    companion object {
        const val ACTION_DEBUG_COMMAND = "dev.wenyu.semanticcontrol.debug.ACTION_HOMEPAGE_COMMAND"
        const val EXTRA_COMMAND = "command"
        private const val TAG = "HomepageDebugReceiver"
    }
}
