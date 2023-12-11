package com.argon.blue.edgelighting

import android.animation.ValueAnimator
import android.graphics.Canvas

abstract class Edge {
    private val handler = android.os.Handler()
    private val drawRunner = Runnable { onDraw() }
    protected var canvas:Canvas?=null
    protected var middleX = 0f
    protected var middleY = 0f
    protected var rotationAnimator = ValueAnimator.ofFloat(0f, 1f)

    abstract fun onDraw()
    public fun startDraw(canv: Canvas) {
        canvas = canv
        invalidate()
        rotationAnimator.start()

    }
    public fun startDraw() {
        invalidate()
        rotationAnimator.start()
    }

    public fun cancelDraw() {
        handler.removeCallbacks(drawRunner)
        rotationAnimator.cancel()
    }

    internal fun invalidate() {
        handler.removeCallbacks(drawRunner)
        handler.post(drawRunner)
    }
}