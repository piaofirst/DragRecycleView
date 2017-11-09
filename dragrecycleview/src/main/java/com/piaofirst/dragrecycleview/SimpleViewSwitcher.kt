package com.piaofirst.dragrecycleview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * Created by gjc on 2017/11/6.
 */
class SimpleViewSwitcher @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        ViewGroup(context, attrs, defStyle) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var count = childCount
        var maxHeight = 0
        var maxWidth = 0
        for (i in 0..(count - 1)) {
            var child = getChildAt(i)
            this.measureChild(child, widthMeasureSpec, heightMeasureSpec)
            maxHeight = child.measuredHeight
            maxWidth = child.measuredWidth
        }
        setMeasuredDimension(maxWidth, maxHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var count = childCount
        for (i in 0..(count - 1)) {
            var child = getChildAt(i)
            if (child.visibility != View.GONE)
                child.layout(0, 0, r - l, b - t)
        }
    }

    fun setView(view: View) {
        if (this.childCount != 0) {
            this.removeViewAt(0)
        }

        this.addView(view, 0)
    }
}
