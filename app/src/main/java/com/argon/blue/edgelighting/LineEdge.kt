package com.argon.blue.edgelighting

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.Log
import android.view.animation.LinearInterpolator

class LineEdge(colors:IntArray, lineWidth:Float, midX:Float, midY:Float, dura:Long) : Edge (){
    private var paintOfLine: Paint? = null
    private var colorsVector:IntArray? = null
    private var paintWidth:Float = 10f
    private var duration = 1000L
    private var gradient: SweepGradient? = null
    private var gradientMatrix = Matrix()


    init{
        colorsVector = colors
        paintWidth = lineWidth
        paintOfLine = Paint(Paint.ANTI_ALIAS_FLAG)
        paintOfLine!!.style = Paint.Style.STROKE
        paintOfLine!!.strokeWidth = paintWidth
        middleX = midX
        middleY = midY
        duration = dura

        rotationAnimator.apply {
            addUpdateListener {
                gradientMatrix.postRotate(-5f, middleX, middleY)
                gradient?.setLocalMatrix(gradientMatrix)
                invalidate()
            }
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
        }
        gradient = SweepGradient(middleX,
            middleY, colorsVector!!, null).apply {
            setLocalMatrix(gradientMatrix)
        }
        paintOfLine!!.shader = gradient
    }

    override fun onDraw() {
        if (canvas != null) {
            canvas!!.drawColor(Color.BLACK)
            canvas!!.drawRoundRect(paintWidth, paintWidth, 2*middleX - paintWidth,
                2*middleY-paintWidth, paintWidth, paintWidth, paintOfLine!!
            )
        }
    }
}