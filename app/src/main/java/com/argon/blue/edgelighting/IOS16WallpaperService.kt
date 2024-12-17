package com.argon.blue.edgelighting

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor


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
        private var dateStartYOffsetInDip = 130f
        private var dateTimeYOffsetIntervalInDip = 24f
        private val dateDefaultTextSizeInSp = 20f
        private val timeDefaultTextSizeInSp = 75f
        private var screenDensity = 0f
        private lateinit var keyguardManager:KeyguardManager

        private var isTimeDateAnimating = false
        private var isTimeDateVisible = false
        private lateinit var timeDateAnimator:ValueAnimator
        private var textDateAlpha = 0f
        private var isScreenLockOn = false
        private var screenStateReceiver:ScreenStateReceiver ?=null
        private var unlockWatchDogRunnable :Runnable?= null
        init {
            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    drawWallpaper(surfaceHolder)
                    handler?.postDelayed(this, 1000)
                }
            }
            unlockWatchDogRunnable = object : Runnable {
                override fun run() {
                    isScreenLockOn = keyguardManager.isKeyguardLocked
                    Log.d("HAHA", "unlockWatchDogRunnable check isScreenLockOn=${isScreenLockOn}")
                    if (!isScreenLockOn) {
                        if(!isTimeDateVisible) {
                            isTimeDateVisible = true
                            startTextDateAnimation()
                        }
                    } else {
                        handler?.postDelayed(this, 500)
                    }
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
                typeface = Typeface.createFromAsset(assets, "fonts/OpenRunde-Bold.otf") // 设置字体样式
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

        inner class ScreenStateReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("HAHA", "onReceive=${intent?.action}")
                if(intent?.action == Intent.ACTION_USER_PRESENT || intent?.action ==Intent.ACTION_SCREEN_ON) {
                    isScreenLockOn = keyguardManager.isKeyguardLocked
                    Log.d("HAHA", "isScreenLockOn=${isScreenLockOn}, isTimeDateVisible=${isTimeDateVisible}")
                    if (!isScreenLockOn && !isTimeDateVisible) {
                        isTimeDateVisible = true
                        startTextDateAnimation()
                    }
                }
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
            wallpaperFgBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample1_fg2, options)
            wallpaperBgBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample1_bg, options)
            runnable?.let { handler?.post(it) }
            keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            screenStateReceiver = ScreenStateReceiver()
            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
            filter.addAction(Intent.ACTION_SCREEN_ON)
            registerReceiver(screenStateReceiver, filter)
            //val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        }

        override fun onDestroy() {
            super.onDestroy()
            wallpaperFgBitmap?.recycle()
            wallpaperFgBitmap = null
            wallpaperBgBitmap?.recycle()
            wallpaperBgBitmap = null
            runnable?.let { handler?.removeCallbacks(it) }
            unregisterReceiver(screenStateReceiver)
        }

        override fun onSurfaceCreated(holder:SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawWallpaper(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible);
            //isTimeDateVisible = visible
            isScreenLockOn = keyguardManager.isKeyguardLocked
            if (visible) {
                runnable?.let { handler?.post(it) }
                //startTextDateAnimation()
            } else {
                runnable?.let { handler?.removeCallbacks(it) }
                //stopTextDateAnimation()
            }
            if (visible) {
                if (!isScreenLockOn) {
                    isTimeDateVisible = true
                    startTextDateAnimation()
                } else {
                    // start a ScreenLockOn watch dog
                    unlockWatchDogRunnable?.let { handler?.postDelayed(it, 500) }
                }
            } else {
                isTimeDateVisible = false
                stopTextDateAnimation()
                unlockWatchDogRunnable?.let { handler?.removeCallbacks(it) }
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

        private fun startTextDateAnimation() {
            if (!isScreenLockOn && !isTimeDateAnimating) {
                isTimeDateAnimating = true
                timeDateAnimator = ValueAnimator.ofFloat(0f,1f)
                timeDateAnimator.duration = 1000 // 1 second
                timeDateAnimator.addUpdateListener { it ->
                    textDateAlpha = it.animatedValue as Float
                    drawWallpaper(surfaceHolder)
                }
                timeDateAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        isTimeDateAnimating = false
                        drawWallpaper(surfaceHolder)
                    }
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
                timeDateAnimator.start()
            }
        }
        private fun stopTextDateAnimation() {
            isTimeDateAnimating = false
            if (timeDateAnimator != null) {
                timeDateAnimator.cancel()
            }
        }

        private fun drawWallpaper(holder:SurfaceHolder){
            val wallpaperFgBitmap = wallpaperFgBitmap?:return
            val wallpaperBgBitmap = wallpaperBgBitmap?:return
            var canvas:Canvas? = null
            isScreenLockOn = keyguardManager.isKeyguardLocked
            try {
                canvas = holder.lockCanvas()
                canvas.drawColor(Color.BLACK)
                // draw background image
                val srcRect = Rect(0, 0, wallpaperFgBitmap.width, wallpaperFgBitmap.height)
                val dstRect = Rect(0, 0, canvas.width, canvas.height)
                canvas.drawBitmap(wallpaperBgBitmap, srcRect, dstRect, null)

                // do not draw time & data while lock screen is on
                if (!isScreenLockOn && isTimeDateVisible) {
                    // draw date & time
                    val time = System.currentTimeMillis()
                    val dateTime = java.util.Date(time)
                    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                    val timeText =
                        android.text.format.DateFormat.format("HH:mm", dateTime).toString()
                    datePaint?.alpha = (255 * textDateAlpha).toInt()
                    datePaint?.let {
                        val dateString: String = dateFormat.format(Date())
                        canvas.drawText(
                            dateString,
                            canvas.width / 2f,
                            screenDensity * dateStartYOffsetInDip + dateDefaultTextSizeInSp * screenDensity / 2,
                            it
                        )
                    }
                    timePaint?.alpha = (255 * textDateAlpha).toInt()
                    timePaint?.let {
                        canvas.drawText(
                            timeText,
                            canvas.width / 2f,
                            dateDefaultTextSizeInSp * screenDensity + (timeDefaultTextSizeInSp * screenDensity) / 2 +
                                    (dateStartYOffsetInDip + dateTimeYOffsetIntervalInDip) * screenDensity,
                            it
                        )
                    }
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

