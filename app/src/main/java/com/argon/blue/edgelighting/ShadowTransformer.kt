package com.argon.blue.edgelighting

import android.view.View
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.ViewPager

class ShadowTransformer(private val viewPager: ViewPager, private val adapter: PreviewPagerAdapter?)
    : ViewPager.PageTransformer, ViewPager.OnPageChangeListener{

    private var mViewPager: ViewPager? = null
    private var mAdapter: PreviewPagerAdapter? = null
    private var mLastOffset = 0f
    private var mScalingEnabled = false

    init {
        mViewPager = viewPager
        viewPager.addOnPageChangeListener(this)
        mAdapter = adapter
    }

    fun enableScaling(enable: Boolean) {
        if (mScalingEnabled && !enable) {
            // shrink main card
            val currentCard: CardView? = mAdapter?.getCardViewAt(mViewPager!!.currentItem)
            if (currentCard != null) {
                currentCard.animate().scaleY(1f)
                currentCard.animate().scaleX(1f)
            }
        } else if (!mScalingEnabled && enable) {
            // grow main card
            val currentCard: CardView ?= mAdapter?.getCardViewAt(mViewPager!!.currentItem)
            if (currentCard != null) {
                currentCard.animate().scaleY(1.1f)
                currentCard.animate().scaleX(1.1f)
            }
        }
        mScalingEnabled = enable
    }

    override fun transformPage(page: View, position: Float) {
        //TODO("Not yet implemented")
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val realCurrentPosition: Int
        val nextPosition: Int
        val baseElevation: Float = mAdapter?.getBaseElevation() ?: 0f
        val realOffset: Float
        val goingLeft = mLastOffset > positionOffset

        // If we're going backwards, onPageScrolled receives the last position
        // instead of the current one

        // If we're going backwards, onPageScrolled receives the last position
        // instead of the current one
        if (goingLeft) {
            realCurrentPosition = position + 1
            nextPosition = position
            realOffset = 1 - positionOffset
        } else {
            nextPosition = position + 1
            realCurrentPosition = position
            realOffset = positionOffset
        }

        // Avoid crash on overscroll

        // Avoid crash on overscroll
        if (nextPosition > mAdapter!!.count - 1
            || realCurrentPosition > mAdapter!!.count - 1
        ) {
            return
        }

        val currentCard: CardView = mAdapter!!.getCardViewAt(realCurrentPosition)
        
        if (currentCard != null) {
            if (mScalingEnabled) {
                currentCard.setScaleX((1 + 0.1 * (1 - realOffset)).toFloat())
                currentCard.setScaleY((1 + 0.1 * (1 - realOffset)).toFloat())
            }
            currentCard.setCardElevation(
                baseElevation + (baseElevation
                        * (CardAdapter.MAX_ELEVATION_FACTOR - 1) * (1 - realOffset))
            )
        }

        val nextCard: CardView = mAdapter!!.getCardViewAt(nextPosition)

        // We might be scrolling fast enough so that the next (or previous) card
        // was already destroyed or a fragment might not have been created yet

        // We might be scrolling fast enough so that the next (or previous) card
        // was already destroyed or a fragment might not have been created yet
        if (nextCard != null) {
            if (mScalingEnabled) {
                nextCard.setScaleX((1 + 0.1 * (realOffset)).toFloat())
                nextCard.setScaleY((1 + 0.1 * (realOffset)).toFloat())
            }
            nextCard.setCardElevation(
                (baseElevation + (baseElevation
                        * (CardAdapter.MAX_ELEVATION_FACTOR - 1) * (realOffset)))
            )
        }

        mLastOffset = positionOffset
    }

    override fun onPageSelected(position: Int) {
        //
    }

    override fun onPageScrollStateChanged(state: Int) {
        //
    }
}