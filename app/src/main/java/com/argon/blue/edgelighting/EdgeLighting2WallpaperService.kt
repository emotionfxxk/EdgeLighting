package com.argon.blue.edgelighting

import android.animation.ValueAnimator
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.animation.LinearInterpolator

class EdgeLighting2WallpaperService  : WallpaperService(){

    override fun onCreateEngine(): WallpaperService.Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private val handler = android.os.Handler();
        private val drawRunner = Runnable { draw() }
        private var surfaceWidth = 0
        private var surfaceHeight = 0
        private var isClockwise = true

        private var pulseGradientColors:IntArray = intArrayOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.RED)
        private var gradient:SweepGradient?= null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var rotationAngle = 0f

        private var edgeWidth: Float = 20f
        private val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val solidBlurRadius = 6f
        private var normalBlurRadius = 6f
        private var cornerRadius = 96f

        init {
            paint.strokeWidth = edgeWidth
            paint.style = Paint.Style.STROKE

            shaderPaint.style = Paint.Style.STROKE
            shaderPaint.strokeWidth = edgeWidth * 1.6f
            shaderPaint.color = Color.BLUE
        }


        var edgeAnimator: ValueAnimator? = null
        private var progressInPercent:Float = 0f
            set(value) {
                field = value
                handler.removeCallbacks(drawRunner)
                handler.post(drawRunner)
            }


        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            canvas = holder.lockCanvas()
            canvas.drawColor(Color.BLACK)
            val width = surfaceWidth.toFloat()
            val height = surfaceHeight.toFloat()
            val rotationMatrix = Matrix()
            rotationMatrix.postRotate(rotationAngle, surfaceWidth/ 2f, surfaceHeight / 2f )
            gradient!!.setLocalMatrix(rotationMatrix)
            // draw border
            paint.shader = gradient
            canvas.drawRoundRect(edgeWidth / 2, edgeWidth / 2,
                width - edgeWidth / 2, height - edgeWidth / 2, cornerRadius, cornerRadius, paint)


            // draw glow
            shaderPaint.shader = gradient
            //shaderPaint.maskFilter = BlurMaskFilter(solidBlurRadius, BlurMaskFilter.Blur.SOLID)
            //shaderPaint.strokeWidth = edgeWidth * 1.4f
            //canvas.drawRoundRect(edgeWidth / 2, edgeWidth / 2,
                //width - edgeWidth / 2, height - edgeWidth / 2, cornerRadius, cornerRadius, shaderPaint)
            shaderPaint.maskFilter = BlurMaskFilter(normalBlurRadius, BlurMaskFilter.Blur.NORMAL)
            shaderPaint.strokeWidth = edgeWidth * 1.2f
            canvas.drawRoundRect(edgeWidth / 2, edgeWidth / 2,
                width - edgeWidth / 2, height - edgeWidth / 2, cornerRadius, cornerRadius, shaderPaint)

            if (canvas != null)
                holder.unlockCanvasAndPost(canvas)
        }


        override fun onVisibilityChanged(visible: Boolean) {
            edgeAnimator?.let {
                if (it.isStarted && !visible){
                    it.cancel()
                }else if (it.isStarted.not() && visible) {
                    it.start()
                }
            }
            super.onVisibilityChanged(visible);
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            edgeAnimator?.cancel()
            super.onSurfaceDestroyed(holder)
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int,
            width: Int, height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height

            gradient = SweepGradient(
                width / 2f,
                height / 2f,
                pulseGradientColors,
                floatArrayOf(0f, 0.25f, 0.5f, 1f)
            )

            edgeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener {
                    progressInPercent = it.animatedValue as Float
                    rotationAngle = if(isClockwise) {
                        progressInPercent * 360f
                    } else {
                        -progressInPercent * 360f
                    }
                }
                duration = 3000L
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
        }


        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
        }
    }
}