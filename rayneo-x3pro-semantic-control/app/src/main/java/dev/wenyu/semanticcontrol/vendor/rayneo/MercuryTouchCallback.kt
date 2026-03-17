package dev.wenyu.semanticcontrol.vendor.rayneo

import com.ffalcon.mercury.android.sdk.touch.CommonTouchCallback
import com.ffalcon.mercury.android.sdk.touch.FlingArgs

class MercuryTouchCallback(
    private val bridge: MercuryTouchSemanticBridge,
) : CommonTouchCallback() {

    override fun onKeyPinch() {
        bridge.onPinch()
    }

    override fun onKeyLongPinch() {
        bridge.onLongPinch()
    }

    override fun onTPClick(): Boolean {
        return bridge.onClick()
    }

    override fun onTPSlideForward(args: FlingArgs): Boolean {
        return bridge.onSlideForward()
    }

    override fun onTPSlideBackward(args: FlingArgs): Boolean {
        return bridge.onSlideBackward()
    }
}
