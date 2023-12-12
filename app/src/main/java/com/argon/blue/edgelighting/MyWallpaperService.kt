package com.argon.blue.edgelighting

import android.animation.ValueAnimator
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.SweepGradient
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import android.view.animation.LinearInterpolator


class MyWallpaperService  : WallpaperService(){

    //private var origWallpaperDrawable:Drawable ?= null


    override fun onCreateEngine(): WallpaperService.Engine {
        //origWallpaperDrawable = getDrawable(R.drawable.wallpaper_sample)
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private val handler = android.os.Handler();
        private val drawRunner = Runnable { draw() }
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var width: Int = 0
        private var height: Int = 0
        private var middleX = 0f
        private var middleY = 0f
        private var visible = true
        private var gradient: SweepGradient? = null
        private var gradientMatrix = Matrix()
        private var rotationAnimator = ValueAnimator.ofFloat(0f, 1f)

        //private var edge:Edge?=null




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
            handler.post(drawRunner)
            paint.setColor(Color.RED)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 20f

            paint.setShadowLayer(40f, 0f, 0f, Color.parseColor("#FF0000"));

            rotationAnimator.apply {
                addUpdateListener {
                    gradientMatrix.postRotate(-5f, middleX, middleY)
                    gradient?.setLocalMatrix(gradientMatrix)
                    invalidate()
                }
                //duration = 1000L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
            }


        }

        private fun invalidate() {
            handler.removeCallbacks(drawRunner)
            if (visible) {
                handler.post(drawRunner)
            }
        }

        private fun draw() {
            Log.d("GG","draw()")
            val holder = surfaceHolder
            var canvas: Canvas? = null

            canvas = holder.lockCanvas()

            if (canvas != null) {
                Log.d("GG","draw color and round rect, width=${width}, height=${height}")
                canvas.drawColor(Color.BLACK)
                //canvas.dr
                gradient = SweepGradient(width.toFloat() / 2,
                    height.toFloat() / 2, COLORS2, null).apply {
                    setLocalMatrix(gradientMatrix)
                }
                paint.shader = gradient
                canvas.drawRoundRect(20f, 20f, width.toFloat() - 20f,
                    height.toFloat()-20f, 20f, 20f, paint)
                //drawWave(canvas)
            }


            if (canvas != null)
                holder.unlockCanvasAndPost(canvas)
        }



        /*
        init {
            val prefs = PreferenceManager
                .getDefaultSharedPreferences(this@MyWallpaperService)
            maxNumber = Integer
                .valueOf(prefs.getString(resources.getString(R.string.lable_number_of_circles), "4")!!)
            touchEnabled = prefs.getBoolean("touch", false)
            circles = ArrayList()
            paint.isAntiAlias = true
            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeWidth = 10f
            handler.post(drawRunner)
        }*/

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {

                handler.post(drawRunner)
                if(rotationAnimator != null)
                    rotationAnimator.start()

            } else {

                handler.removeCallbacks(drawRunner)
                if(rotationAnimator != null)
                    rotationAnimator.cancel()

            }
            super.onVisibilityChanged(visible);
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            this.visible = false

            handler.removeCallbacks(drawRunner)
            if(rotationAnimator != null)
                rotationAnimator.cancel()

        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int,
            width: Int, height: Int
        ) {

            this.width = width
            this.height = height

            middleX = this.width / 2f
            middleY = this.height / 2f


            super.onSurfaceChanged(holder, format, width, height)
        }

        /*
        override fun onTouchEvent(event: MotionEvent) {
            if (touchEnabled) {

                val x = event.x
                val y = event.y
                val holder = surfaceHolder
                var canvas: Canvas? = null

                canvas = holder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK)
                    circles.clear()
                    circles.add(MyPoint((circles.size + 1).toString(), x, y))
                    drawCircles(canvas, circles)

                }

                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas)

                super.onTouchEvent(event)
            }
        }*/
    }
}