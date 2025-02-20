package com.argon.blue.edgelighting


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.argon.blue.edgelighting.data.WallpaperData
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DepthWallpaperPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var dateTextView:TextView
    private var timeTextView:TextView
    private var bgImage:ImageView
    private var fgImage:ImageView

    private var handler: Handler? = null
    private var dateTimeUpdater: Runnable? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }
    private val path = Path()
    private var cornerRadius = 20f
    private var wallpaper: WallpaperData ?= null

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.depth_wallpaer_preview, this, true)
        dateTextView = findViewById(R.id.date)
        timeTextView = findViewById(R.id.time)
        bgImage = findViewById(R.id.bg)
        fgImage = findViewById(R.id.fg)

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                Log.d("SEAN", "w=${width}, h=${height}")
                val bitmap = loadScaledImageFromAssets(context, wallpaper!!.backImage.url,
                    bgImage.width, bgImage.height)
                if (bitmap != null) {
                    bgImage.setImageBitmap(bitmap)
                }
                val fgBitmap = loadScaledImageFromAssets(context, wallpaper!!.frontImage.url,
                    fgImage.width, fgImage.height)
                if (bitmap != null) {
                    fgImage.setImageBitmap(fgBitmap)
                }
                updateDateTimePosY(fgImage.drawable)
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        var dateTypeface: Typeface? = null
        var timeTypeface: Typeface? = null
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DepthWallpaperPreview)
        try {

            val dateFontPath = typedArray.getString(R.styleable.DepthWallpaperPreview_dateFont)
            if (dateFontPath!= null) {
                dateTypeface = Typeface.createFromAsset(context.assets, dateFontPath)
            }
            val timeFontPath = typedArray.getString(R.styleable.DepthWallpaperPreview_timeFont)
            if (timeFontPath!= null) {
                timeTypeface = Typeface.createFromAsset(context.assets, timeFontPath)
            }

            dateTextView.setTextColor(
                typedArray.getColor(R.styleable.DepthWallpaperPreview_dateTextColor, Color.WHITE))
            timeTextView.setTextColor(
                typedArray.getColor(R.styleable.DepthWallpaperPreview_timeTextColor, Color.WHITE))

            dateTextView.textSize =
                typedArray.getDimension(R.styleable.DepthWallpaperPreview_dateFontSize,
                    18f) / resources.displayMetrics.scaledDensity
            timeTextView.textSize =
                typedArray.getDimension(R.styleable.DepthWallpaperPreview_timeFontSize,
                    48f) / resources.displayMetrics.scaledDensity

            cornerRadius =
                typedArray.getDimensionPixelSize(R.styleable.DepthWallpaperPreview_cornerRadius, 36)
                    .toFloat()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
        if ( dateTypeface != null ) {
            dateTextView.typeface = dateTypeface
        }
        if ( timeTypeface != null ) {
            timeTextView.typeface = timeTypeface
        }

        handler = Handler(Looper.getMainLooper())
        dateTimeUpdater = object : Runnable {
            override fun run() {
                val time = System.currentTimeMillis()
                val dateTime = Date(time)
                val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                val timeText =
                    android.text.format.DateFormat.format("HH:mm", dateTime).toString()
                timeTextView.text = timeText
                dateTextView.text = dateFormat.format(Date())
                handler?.postDelayed(this, 1000)
            }
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.reset()
        path.addRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()),
            cornerRadius, cornerRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        super.onDraw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restore()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        dateTimeUpdater?.let { handler?.post(it) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dateTimeUpdater?.let { handler?.removeCallbacks(it) }
    }

    fun updateWallpaperData(wallpaperData:WallpaperData) {
        wallpaper = wallpaperData
    }

    private fun loadScaledImageFromAssets(context: Context, fileName: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val assetManager = context.assets
            // 第一次解码仅获取图片的尺寸信息
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            inputStream = assetManager.open(fileName)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // 计算缩放比例
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // 第二次解码，根据缩放比例加载图片
            options.inJustDecodeBounds = false
            inputStream = assetManager.open(fileName)
            return BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        Log.d("SEAN", "reqH=${reqHeight}, reqW=${reqWidth}, w=${width}, h=${height}")
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun updateDateTimePosY(drawable: Drawable) {
        val overlapHeight = ((dateTextView.height + timeTextView.height) * 0.05f).toInt()

        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val transparentTopHeight = getTransparentTopHeight(bitmap)

            if(dateTextView.layoutParams is RelativeLayout.LayoutParams) {
                (dateTextView.layoutParams as LayoutParams).topMargin = transparentTopHeight + overlapHeight -
                        dateTextView.height - timeTextView.height
            }
        }
    }

    private fun getTransparentTopHeight(bitmap: Bitmap): Int {
        val width = bitmap.width
        val height = bitmap.height
        var transparentHeight = 0
        var allTransparent = true
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = Color.alpha(pixel)
                if (alpha!= 0) {
                    allTransparent = false
                    break
                }
            }
            if (!allTransparent) {
                transparentHeight = y
                break
            }
        }
        if (allTransparent) {
            transparentHeight = height
        }
        return transparentHeight
    }
}