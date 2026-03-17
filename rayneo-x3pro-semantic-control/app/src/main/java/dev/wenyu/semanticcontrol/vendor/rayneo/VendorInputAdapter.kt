package dev.wenyu.semanticcontrol.vendor.rayneo

import android.view.KeyEvent
import android.view.MotionEvent

interface VendorInputAdapter {
    fun onMotionEvent(event: MotionEvent)

    fun onKeyEvent(event: KeyEvent)
}
