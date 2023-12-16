package com.argon.blue.edgelighting

import android.animation.ValueAnimator
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SweepGradient
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import android.view.animation.LinearInterpolator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class IOS16WallpaperService  : WallpaperService(){


    override fun onCreateEngine(): WallpaperService.Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private var wallpaperFgBitmap : Bitmap ?= null
        private var wallpaperBgBitmap : Bitmap ?= null
        private var timePaint:Paint ?= null
        private var datePaint:Paint ?= null
        private var handler: Handler? = null
        private var runnable: Runnable? = null
        private var alphaOfFont:Int = 230 // 90% alpha
        private var dateStartYOffsetInDip = 96f
        private var dateTimeYOffsetIntervalInDip = 24f
        private val dateDefaultTextSizeInSp = 20f
        private val timeDefaultTextSizeInSp = 75f
        private var screenDensity = 0f
        init {
            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    drawWallpaper(surfaceHolder)
                    handler?.postDelayed(this, 1000)
                }
            }
            screenDensity = resources.displayMetrics.density
            timePaint = Paint().apply {
                color = Color.WHITE
                alpha = alphaOfFont
                textSize = timeDefaultTextSizeInSp * screenDensity
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                //typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) // 设置字体样式
                typeface = Typeface.createFromAsset(assets, "fonts/NeverMindRounded-Bold.ttf") // 设置字体样式
            }
            datePaint = Paint().apply {
                color = Color.WHITE
                alpha = alphaOfFont
                textSize = dateDefaultTextSizeInSp * screenDensity
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                //typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) // 设置字体样式
                typeface = Typeface.createFromAsset(assets, "fonts/SulphurPoint-Regular.otf") // 设置字体样式
            }

        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.sample1_bg, options)
            val wallpaperWidth = options.outWidth
            val wallpaperHeight = options.outHeight
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val scaleFactor = maxOf(wallpaperWidth / screenWidth, wallpaperHeight / screenHeight)
            options.inJustDecodeBounds = false
            options.inSampleSize = scaleFactor
            wallpaperFgBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample1_fg, options)
            wallpaperBgBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample1_bg, options)
            runnable?.let { handler?.post(it) }
        }

        override fun onDestroy() {
            super.onDestroy()
            wallpaperFgBitmap?.recycle()
            wallpaperFgBitmap = null
            wallpaperBgBitmap?.recycle()
            wallpaperBgBitmap = null
            runnable?.let { handler?.removeCallbacks(it) }
        }

        override fun onSurfaceCreated(holder:SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawWallpaper(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible);
            if (visible) {
                runnable?.let { handler?.post(it) }
            } else {
                runnable?.let { handler?.removeCallbacks(it) }
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)

        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int,
            width: Int, height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            drawWallpaper(holder)
        }

        private fun drawWallpaper(holder:SurfaceHolder){
            val wallpaperFgBitmap = wallpaperFgBitmap?:return
            val wallpaperBgBitmap = wallpaperBgBitmap?:return
            var canvas:Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas.drawColor(Color.BLACK)
                // draw background image
                val srcRect = Rect(0, 0, wallpaperFgBitmap.width, wallpaperFgBitmap.height)
                val dstRect = Rect(0, 0, canvas.width, canvas.height)
                canvas.drawBitmap(wallpaperBgBitmap, srcRect, dstRect, null)

                // draw date & time
                val time = System.currentTimeMillis()
                val dateTime = java.util.Date(time)
                val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                val timeText = android.text.format.DateFormat.format("HH:mm", dateTime).toString()
                datePaint?.let {
                    val dateString:String = dateFormat.format(Date())
                    canvas.drawText(dateString, canvas.width / 2f,
                        screenDensity * dateStartYOffsetInDip + dateDefaultTextSizeInSp * screenDensity / 2, it
                    )
                }
                timePaint?.let {
                    canvas.drawText(
                        timeText,
                        canvas.width / 2f,
                        dateDefaultTextSizeInSp * screenDensity + (timeDefaultTextSizeInSp * screenDensity) / 2 +
                                (dateStartYOffsetInDip + dateTimeYOffsetIntervalInDip) * screenDensity,
                        it
                    )
                }

                // draw foreground image
                canvas.drawBitmap(wallpaperFgBitmap, srcRect, dstRect, null)
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
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