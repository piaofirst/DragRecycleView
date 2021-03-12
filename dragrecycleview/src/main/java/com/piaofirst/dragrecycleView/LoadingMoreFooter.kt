package com.piaofirst.dragrecycleView

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.piaofirst.dragrecycleView.progressindicator.AVLoadingIndicatorView

/**
 * Created by Administrator on 2017/11/7.
 */
class LoadingMoreFooter @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        LinearLayout(context, attrs, defStyle) {
    companion object {
        const val STATE_LAODING = 0
        const val STATE_COMPLETE = 1
        const val STATE_NOMORE = 2
    }

    private var progressContent: SimpleViewSwitcher? = null
    private var mText: TextView? = null

    init {
        gravity = Gravity.CENTER
        layoutParams = LayoutParams(ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        progressContent = SimpleViewSwitcher(context)
        progressContent?.layoutParams = LayoutParams(
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        var progressView = AVLoadingIndicatorView(context)
        progressView.setIndicatorColor(0xffB5B5B5.toInt())
        progressView.setIndicator(ProgressStyle.BallSpinFadeLoaderIndicator)
        progressContent?.setView(progressView)

        addView(progressContent)
        mText = TextView(context)
        mText?.text = "正在加载..."

        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val dimension = resources.getDimension(R.dimen.textAndIconMargin).toInt()
        layoutParams.setMargins(dimension, dimension, dimension, dimension)
        mText?.layoutParams = layoutParams
        addView(mText)
    }

    fun setProgressStyle(style: String) {
        if (style == ProgressStyle.SysProgress) {
            progressContent?.setView(ProgressBar(context, null, android.R.attr.progressBarStyle))
        } else {
            var progressView = AVLoadingIndicatorView(context)
            progressView.setIndicatorColor(0xffB5B5B5.toInt())
            progressView.setIndicator(style)
            progressContent?.setView(progressView)
        }
    }

    fun setState(state: Int) {
        when (state) {
            STATE_LAODING -> {
                progressContent?.visibility = View.VISIBLE
                mText?.text = context.getText(R.string.loading)
                this.visibility = View.VISIBLE
            }
            STATE_COMPLETE -> {
                mText?.setText(R.string.loading)
                this.visibility = View.GONE
            }
            STATE_NOMORE -> {
                mText?.setText(R.string.nomore_loading)
                progressContent?.visibility = View.GONE
                this.visibility = View.VISIBLE
            }
        }
    }
}
