package com.argon.blue.edgelighting

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat

class SqueezWallpaperService  : WallpaperService(){

    override fun onCreateEngine(): WallpaperService.Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private val handler = android.os.Handler();
        private val drawRunner = Runnable { draw() }

        var mSqueezebarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF0000")
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 20f
        }

        var horizontalWidth = 0
        var surfaceWidth = 0
        var surfaceHeight = 0
        var horizontalSqueezHeight = 20f
        var isClockwise = true

        var mSqueezebarAnimator: ValueAnimator? = null
        private var mSqueezebarDisplacement = 0f
            set(value) {
                field = value
                handler.removeCallbacks(drawRunner)
                handler.post(drawRunner)
            }
        private var mAnimationFraction = 0f


        private val COLORS2 = intArrayOf(
            Color.parseColor("#33004c"), Color.parseColor("#4600d2"),
            Color.parseColor("#0000ff"), Color.parseColor("#0099ff"),
            Color.parseColor("#00eeff"), Color.parseColor("#00FF7F"),
            Color.parseColor("#48FF00"), Color.parseColor("#B6FF00"),
            Color.parseColor("#FFD700"), Color.parseColor("#ff9500"),
            Color.parseColor("#FF6200"), Color.parseColor("#FF0000"),
            Color.parseColor("#33004c")
        )

        init {

            //TODO

        }

        fun drawHorizontalSqueezebar(canvas: Canvas, centerPositionX: Float, squeezeFactor: Float) {
            // 1. draw top squeez bar
            val leftPositionX = centerPositionX - squeezeFactor*horizontalWidth/2f
            val rightPositionX = centerPositionX + squeezeFactor*horizontalWidth/2f
            val rectTop = RectF().apply {
                left = leftPositionX
                top = 0F
                right = rightPositionX
                bottom = horizontalSqueezHeight
            }
            canvas.drawRect(rectTop, mSqueezebarPaint)
            // 2. draw bottom squeez bar

            val rectBottom = RectF().apply {
                top = surfaceHeight - horizontalSqueezHeight
                right = surfaceWidth - leftPositionX
                left = right - rightPositionX + leftPositionX
                bottom = surfaceHeight.toFloat()
            }
            canvas.drawRect(rectBottom, mSqueezebarPaint)
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            canvas = holder.lockCanvas()
            canvas.drawColor(Color.BLACK)
            val currentCenterPositionX = mSqueezebarDisplacement
            val squeezeFactor = if(mAnimationFraction <= 0.5) mAnimationFraction else 1-mAnimationFraction
            Log.d("HAHA", "squeezeFactor=${squeezeFactor}, " +
                    "mAnimationFraction=${mAnimationFraction}" + ",mSqueezebarDisplacement=${mSqueezebarDisplacement}")
            drawHorizontalSqueezebar(canvas, currentCenterPositionX, squeezeFactor)

            if (canvas != null)
                holder.unlockCanvasAndPost(canvas)
        }


        override fun onVisibilityChanged(visible: Boolean) {
            mSqueezebarAnimator?.let {
                if (it.isStarted && !visible){
                    it.cancel()
                }else if (it.isStarted.not() && visible) {
                    it.start()
                }
            }
            super.onVisibilityChanged(visible);
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            mSqueezebarAnimator?.cancel()
            super.onSurfaceDestroyed(holder)
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int,
            width: Int, height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height
            horizontalWidth = width
            mSqueezebarAnimator = ValueAnimator.ofFloat(0f, horizontalWidth.toFloat()).apply {
                addUpdateListener {
                    mSqueezebarDisplacement = (it.animatedValue as Float)
                    mAnimationFraction = it.animatedFraction
                }
                duration = 1000L
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                interpolator = EaseInOutCubicInterpolator()
                start()
            }
        }


        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
        }
    }
}