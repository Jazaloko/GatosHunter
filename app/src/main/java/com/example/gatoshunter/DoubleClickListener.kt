package com.example.gatoshunter

import android.view.View

abstract class DoubleClickListener : View.OnClickListener {
    private var lastClickTime: Long = 0
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300 // milisegundos

    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
        }
        lastClickTime = clickTime
    }

    abstract fun onDoubleClick(v: View)
}
