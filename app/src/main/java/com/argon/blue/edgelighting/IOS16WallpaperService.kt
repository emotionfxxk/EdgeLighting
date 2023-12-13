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
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import android.view.animation.LinearInterpolator


class IOS16WallpaperService  : WallpaperService(){


    override fun onCreateEngine(): WallpaperService.Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private var wallpaperFgBitmap : Bitmap ?= null
        private var wallpaperBgBitmap : Bitmap ?= null

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
        }

        override fun onDestroy() {
            super.onDestroy()
            wallpaperFgBitmap?.recycle()
            wallpaperFgBitmap = null
            wallpaperBgBitmap?.recycle()
            wallpaperBgBitmap = null
        }

        override fun onSurfaceCreated(holder:SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawWallpaper(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible);
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
                val srcRect = Rect(0, 0, wallpaperFgBitmap.width, wallpaperFgBitmap.height)
                val dstRect = Rect(0, 0, canvas.width, canvas.height)
                canvas.drawBitmap(wallpaperBgBitmap, srcRect, dstRect, null)

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