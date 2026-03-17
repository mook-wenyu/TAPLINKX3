package dev.wenyu.semanticcontrol.vendor.rayneo

import android.view.KeyEvent
import android.view.MotionEvent
import com.ffalcon.mercury.android.sdk.touch.TouchDispatcher
import com.ffalcon.mercury.android.sdk.touch.TouchDispatcherX3

class MercuryTouchInputAdapter(
    onSemanticAction: (dev.wenyu.semanticcontrol.core.contracts.SemanticAction) -> Boolean,
) : VendorInputAdapter {

    private val callback = MercuryTouchCallback(MercuryTouchSemanticBridge(onSemanticAction))
    private val dispatcher = TouchDispatcherX3(TouchDispatcher.Source.Activity)

    override fun onMotionEvent(event: MotionEvent) {
        dispatcher.onMotionEvent(event, callback)
    }

    override fun onKeyEvent(event: KeyEvent) {
        dispatcher.onKeyEvent(event, callback)
    }
}
