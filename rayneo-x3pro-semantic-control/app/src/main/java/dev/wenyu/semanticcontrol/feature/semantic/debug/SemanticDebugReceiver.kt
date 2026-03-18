package dev.wenyu.semanticcontrol.feature.semantic.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dev.wenyu.semanticcontrol.feature.semantic.SemanticAccessibilityService

class SemanticDebugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = SemanticAccessibilityService.activeInstance
        if (service == null) {
            setResultCode(2)
            setResultData("service-unavailable")
            Log.w(TAG, "Received debug command without active accessibility service")
            return
        }

        val command = SemanticDebugCommand.fromWireValue(intent.getStringExtra(EXTRA_COMMAND))
        if (command == null) {
            setResultCode(3)
            setResultData("unknown-command")
            Log.w(TAG, "Received unknown debug command")
            return
        }

        val result = service.handleDebugCommand(command)
        setResultCode(if (result.success) 0 else 1)
        setResultData(result.message)
        Log.i(TAG, "Debug command ${command.wireValue} -> ${result.message}")
    }

    companion object {
        const val ACTION_DEBUG_COMMAND = "dev.wenyu.semanticcontrol.debug.ACTION_COMMAND"
        const val EXTRA_COMMAND = "command"
        private const val TAG = "SemanticDebugReceiver"
    }
}
