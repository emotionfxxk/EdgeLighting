package com.argon.blue.edgelighting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.argon.blue.edgelighting.CardAdapter.Companion.MAX_ELEVATION_FACTOR
import com.argon.blue.edgelighting.data.WallpaperData


class PreviewPagerAdapter(private val dataList: List<WallpaperData>) : PagerAdapter(), CardAdapter {
    private var mViews: Array<CardView?>
    private var mBaseElevation = 0f

    init {
        val n:Int = dataList.size
        mViews = Array(size = n) { null }
    }

    override fun getBaseElevation(): Float {
        return mBaseElevation
    }

    override fun getCardViewAt(position: Int): CardView {
        return mViews[position]!!
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val cardView = inflater.inflate(R.layout.preview_card, container, false) as CardView
        val preview = cardView.findViewById<DepthWallpaperPreview>(R.id.preview)
        val name = cardView.findViewById<TextView>(R.id.cardName)
        container.addView(cardView)
        if (mBaseElevation == 0f) {
            mBaseElevation = cardView.getCardElevation()
        }
        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR)
        mViews[position] = cardView
        preview.updateWallpaperData(dataList[position])
        name.text = dataList[position].name
        return cardView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}