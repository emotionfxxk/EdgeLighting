package com.argon.blue.edgelighting

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import kotlin.random.Random


class MatrixLiveWallpaperService  : WallpaperService(){


    override fun onCreateEngine(): WallpaperService.Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : WallpaperService.Engine() {
        private var matrixChars : String = "゠アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレワヰヱヲンヺ・ーヽヿ0123456789"
        //private var defaultColor : Color = 0x00FF44
        private var rainbowSpeed : Float = 1.75f
        private var rainbowOn : Boolean = true
        private var rainbowLightness: Int = 60
        private var rainbowSaturation: Int = 100
        private var speedInMillion:Long = 60
        private var matrixTextSize:Float =  12f // size in dip
        private var columns:Int = 0
        private var chWidth:Float = 0f
        private var drops = mutableListOf<Float>()
        private var stringMatrix  = mutableListOf<String>()


        private var matrixPaint:Paint
        private var handler: Handler? = null
        private var runnable: Runnable? = null
        private var gradient:LinearGradient ? = null
        private val matrix = Matrix()

        private var screenDensity = 0f
        init {

            screenDensity = resources.displayMetrics.density
            matrixPaint = Paint().apply {
                color = Color.GREEN
                textSize = matrixTextSize * screenDensity
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) // 设置字体样式
            }
            gradient = LinearGradient(
                resources.displayMetrics.widthPixels / 2f,
                0f,
                resources.displayMetrics.widthPixels / 2f,
                resources.displayMetrics.heightPixels / 2f,
                intArrayOf(Color.TRANSPARENT, Color.GREEN),
                null,
                Shader.TileMode.CLAMP
            )
            matrixPaint.shader = gradient
            chWidth = matrixPaint?.measureText(matrixChars.get(0).toString())!!
            columns = (resources.displayMetrics.widthPixels / chWidth).toInt()
            val range = resources.displayMetrics.heightPixels / matrixPaint.textSize
            val strLengthOfMatrix = (range / 1.5f).toInt()
            for(i in 0 until columns) {
                drops.add((Math.floor(Math.random() * range) + 1).toFloat())
                val initCharSet = CharArray(strLengthOfMatrix)
                for(j in 0 until strLengthOfMatrix ){
                    initCharSet[j] = matrixChars.get(Random.nextInt(matrixChars.length))

                }
                stringMatrix.add(String(initCharSet))
            }

            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    drawMatrix(surfaceHolder)
                    handler?.postDelayed(this, speedInMillion)
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            runnable?.let { handler?.post(it) }
        }

        override fun onDestroy() {
            super.onDestroy()
            runnable?.let { handler?.removeCallbacks(it) }
        }

        override fun onSurfaceCreated(holder:SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawMatrix(holder)
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
            drawMatrix(holder)
        }

        private fun drawMatrix(holder:SurfaceHolder){
            var canvas:Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas.drawColor(Color.BLACK)

                for (col in 0 until columns) {
                    val xPos = chWidth / 2f + chWidth * col
                    var yPos: Float = matrixPaint.textSize / 2f + drops[col] * matrixPaint.textSize
                    gradient?.setLocalMatrix(matrix.apply {
                        setTranslate(0f, yPos)
                    })
                    var strCol = stringMatrix.get(col)
                    for (i in 0 until strCol.length) {
                        var ch = strCol[i].toString()
                        var chWidth = matrixPaint?.measureText(ch)
                        canvas.drawText(ch, xPos, yPos, matrixPaint)
                        yPos += matrixPaint.textSize
                    }
                    drops[col] = drops[col] + 1
                    if(drops[col] * matrixPaint.textSize > resources.displayMetrics.heightPixels) {
                        drops[col] = - strCol.length.toFloat();
                    }
                }

            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
        }
    }
}