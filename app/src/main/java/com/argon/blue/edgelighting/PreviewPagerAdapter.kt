package com.argon.blue.edgelighting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.argon.blue.edgelighting.CardAdapter.Companion.MAX_ELEVATION_FACTOR


class PreviewPagerAdapter(private val dataList: List<String>) : PagerAdapter(), CardAdapter {
    //private var mViews: ArrayList<CardView> = ArrayList()
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
        container.addView(cardView)
        if (mBaseElevation == 0f) {
            mBaseElevation = cardView.getCardElevation()
        }
        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR)
        mViews[position] = cardView
        return cardView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}