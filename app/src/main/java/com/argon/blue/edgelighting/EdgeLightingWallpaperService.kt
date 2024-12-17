package com.argon.blue.edgelighting

import android.animation.ValueAnimator
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.animation.LinearInterpolator

class EdgeLightingWallpaperService  : WallpaperService(){

    override fun onCreateEngine(): WallpaperService.Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private val handler = android.os.Handler();
        private val drawRunner = Runnable { draw() }
        private var surfaceWidth = 0
        private var surfaceHeight = 0
        private var isClockwise = true

        private var topGradient:LinearGradient?=null
        private var bottomGradient:LinearGradient?=null
        private var rightGradient:LinearGradient?=null
        private var leftGradient:LinearGradient?=null
        private var pulseLength:Float = 192f

        private var pulseGradientColors:IntArray = intArrayOf(
            Color.parseColor("#0000FF00"),
            Color.parseColor("#FF00FF00"),
            Color.parseColor("#0000FF00"))

        private var baselineColor:Int = Color.parseColor("#7F00FF00")
        private var edgeWidth: Float = 20f
        private val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val solidBlurRadius = 6f
        private var normalBlurRadius = 18f
        private var cornerRadius = 16f

        init {
            // 设置 Paint 的属性
            shaderPaint.style = Paint.Style.STROKE
            shaderPaint.strokeWidth = edgeWidth
            shaderPaint.color = baselineColor

            borderPaint.style = Paint.Style.STROKE
            borderPaint.strokeWidth = edgeWidth
            borderPaint.color = baselineColor
            borderPaint.maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL)
        }


        var edgeAnimator: ValueAnimator? = null
        private var progressInPercent:Float = 0f
            set(value) {
                field = value
                val topStartX = surfaceWidth * progressInPercent - pulseLength / 2
                topGradient = LinearGradient(topStartX, edgeWidth / 2, topStartX + pulseLength , edgeWidth / 2,
                    pulseGradientColors,
                    null,
                    Shader.TileMode.CLAMP
                )
                val bottomStartX = surfaceWidth * (1 - progressInPercent) - pulseLength / 2
                bottomGradient = LinearGradient(bottomStartX, surfaceHeight.toFloat() - edgeWidth / 2 ,
                    bottomStartX + pulseLength , surfaceHeight.toFloat() - edgeWidth / 2,
                    pulseGradientColors,
                    null,
                    Shader.TileMode.CLAMP
                )
                val rightStartY = surfaceHeight * progressInPercent - pulseLength / 2
                rightGradient = LinearGradient(surfaceWidth  - edgeWidth / 2, rightStartY,
                    surfaceWidth  - edgeWidth / 2 , rightStartY + pulseLength,
                    pulseGradientColors,
                    null,
                    Shader.TileMode.CLAMP
                )
                val leftStartY = surfaceHeight * (1-progressInPercent) - pulseLength / 2
                leftGradient = LinearGradient(surfaceWidth  - edgeWidth / 2, leftStartY,
                    surfaceWidth  - edgeWidth / 2 , leftStartY + pulseLength,
                    pulseGradientColors,
                    null,
                    Shader.TileMode.CLAMP
                )
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

            canvas.drawRoundRect(edgeWidth / 2, edgeWidth / 2,
                width - edgeWidth / 2, height - edgeWidth / 2, cornerRadius, cornerRadius, borderPaint)

            // draw the top line glow
            shaderPaint.shader = topGradient
            shaderPaint.maskFilter = BlurMaskFilter(solidBlurRadius, BlurMaskFilter.Blur.SOLID)
            shaderPaint.strokeWidth = edgeWidth * 1.4f
            canvas.drawLine(0f, edgeWidth / 2, width, edgeWidth / 2, shaderPaint)

            shaderPaint.maskFilter = BlurMaskFilter(normalBlurRadius, BlurMaskFilter.Blur.NORMAL)
            shaderPaint.strokeWidth = edgeWidth * 2f
            canvas.drawLine(0f, edgeWidth / 2, width, edgeWidth / 2, shaderPaint)

            // draw bottom line glow
            shaderPaint.shader = bottomGradient
            shaderPaint.maskFilter = BlurMaskFilter(solidBlurRadius, BlurMaskFilter.Blur.SOLID)
            shaderPaint.strokeWidth = edgeWidth * 1.4f
            canvas.drawLine(0f, height - edgeWidth / 2, width, height - edgeWidth / 2, shaderPaint)

            shaderPaint.maskFilter = BlurMaskFilter(normalBlurRadius, BlurMaskFilter.Blur.NORMAL)
            shaderPaint.strokeWidth = edgeWidth * 2f
            canvas.drawLine(0f, height - edgeWidth / 2, width, height - edgeWidth / 2, shaderPaint)

            // draw right glow

            shaderPaint.shader = rightGradient
            shaderPaint.maskFilter = BlurMaskFilter(solidBlurRadius, BlurMaskFilter.Blur.SOLID)
            shaderPaint.strokeWidth = edgeWidth * 1.4f
            canvas.drawLine(width - edgeWidth / 2, 0f + edgeWidth, width - edgeWidth / 2, height - edgeWidth, shaderPaint)

            shaderPaint.maskFilter = BlurMaskFilter(normalBlurRadius, BlurMaskFilter.Blur.NORMAL)
            shaderPaint.strokeWidth = edgeWidth * 2f
            canvas.drawLine(width - edgeWidth / 2, 0f + edgeWidth, width - edgeWidth / 2, height - edgeWidth, shaderPaint)

            // draw left glow
            shaderPaint.shader = leftGradient
            shaderPaint.maskFilter = BlurMaskFilter(solidBlurRadius, BlurMaskFilter.Blur.SOLID)
            shaderPaint.strokeWidth = edgeWidth * 1.4f
            canvas.drawLine(edgeWidth / 2, 0f + edgeWidth, edgeWidth / 2, height - edgeWidth, shaderPaint)

            shaderPaint.maskFilter = BlurMaskFilter(normalBlurRadius, BlurMaskFilter.Blur.NORMAL)
            shaderPaint.strokeWidth = edgeWidth * 2f
            canvas.drawLine(edgeWidth / 2, 0f + edgeWidth, edgeWidth / 2, height - edgeWidth, shaderPaint)


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
            //pulseLength = width.toFloat()
            edgeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener {
                    progressInPercent = it.animatedValue as Float
                }
                duration = 1600L
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