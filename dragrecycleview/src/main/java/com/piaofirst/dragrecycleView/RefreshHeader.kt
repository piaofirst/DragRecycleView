package com.piaofirst.dragrecycleView

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.piaofirst.dragrecycleView.BaseRefreshHeader.Companion.STATE_DONE
import com.piaofirst.dragrecycleView.BaseRefreshHeader.Companion.STATE_NORMAL
import com.piaofirst.dragrecycleView.BaseRefreshHeader.Companion.STATE_REFRESHING
import com.piaofirst.dragrecycleView.BaseRefreshHeader.Companion.STATE_RELEASE_TO_REFRESH
import com.piaofirst.dragrecycleView.progressindicator.AVLoadingIndicatorView
import kotlinx.android.synthetic.main.refreshview_header.view.*
import java.util.*

/**
 * Created by gjc on 2017/11/6.
 */
class RefreshHeader @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        LinearLayout(context, attrs, defStyle), BaseRefreshHeader {

    private var mContainer: LinearLayout? = null

    private var mRotateUpAnim: Animation? = null
    private var mRotateDownAnim: Animation? = null
    private val ROTATE_ANIM_DURATION = 180
    var mMeasuredHeight: Int = 0
    private var mState = STATE_NORMAL


    init {
        //初始化 设置下拉刷新view高度为0
        mContainer = LayoutInflater.from(context).inflate(R.layout.refreshview_header, null) as LinearLayout
        var lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, 0)
        this.layoutParams = lp
        this.setPadding(0, 0, 0, 0)
        addView(mContainer, LayoutParams(LayoutParams.MATCH_PARENT, 0))
        gravity = Gravity.BOTTOM

        var progressView = AVLoadingIndicatorView(context)
        progressView.setIndicatorColor(0xffB5B5B5.toInt())
        progressView.setIndicator(ProgressStyle.BallSpinFadeLoaderIndicator)
        header_progressbar.setView(progressView)

        mRotateUpAnim = RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        mRotateUpAnim?.duration = ROTATE_ANIM_DURATION.toLong()
        mRotateUpAnim?.fillAfter = true
        mRotateDownAnim = RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        mRotateDownAnim?.duration = ROTATE_ANIM_DURATION.toLong()
        mRotateDownAnim?.fillAfter = true
        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mMeasuredHeight = measuredHeight
    }

    fun setProgressStyle(style: String) {
        if (style == ProgressStyle.SysProgress) {
            header_progressbar.setView(ProgressBar(context, null, android.R.attr.progressBarStyle))
        } else {
            var progressView = AVLoadingIndicatorView(context)
            progressView.setIndicatorColor(0xffB5B5B5.toInt())
            progressView.setIndicator(style)
            header_progressbar.setView(progressView)
        }
    }

    fun setHeaderArrow(resId: Int) {
        header_arrow.setImageResource(resId)
    }

    fun setState(state: Int) {
        if (state == mState) return

        when (state) {
            STATE_REFRESHING -> { //显示进度
                header_arrow.clearAnimation()
                header_arrow.visibility = View.INVISIBLE
                header_progressbar.visibility = View.VISIBLE
            }
            STATE_DONE -> {
                header_arrow.visibility = View.INVISIBLE
                header_progressbar.visibility = View.INVISIBLE
            }
            else -> { // 显示箭头图片
                header_arrow.visibility = View.VISIBLE
                header_progressbar.visibility = View.INVISIBLE
            }
        }

        when (state) {
            STATE_NORMAL -> {
                if (mState == STATE_RELEASE_TO_REFRESH) {
                    header_arrow.startAnimation(mRotateDownAnim)
                }
                if (mState == STATE_REFRESHING) {
                    header_arrow.clearAnimation()
                }
                refresh_status_textview.setText(R.string.header_hint_normal)
            }
            STATE_RELEASE_TO_REFRESH -> {
                if (mState != STATE_RELEASE_TO_REFRESH) {
                    header_arrow.clearAnimation()
                    header_arrow.startAnimation(mRotateUpAnim)
                    refresh_status_textview.setText(R.string.header_hint_release)
                }
            }
            STATE_REFRESHING -> {
                refresh_status_textview.setText(R.string.refreshing)
            }
            STATE_DONE -> {
                refresh_status_textview.setText(R.string.refresh_done)
            }
            else -> {
            }
        }
        mState = state
    }

    fun getState(): Int {
        return mState
    }

    override fun onMove(delta: Float, mLoadingListener: DragRecycleView.LoadingListener?) {
        if (getVisiableHeight() > 0 || delta > 0) {
            setVisiableHeight((delta + getVisiableHeight()).toInt())
            if (mState <= STATE_RELEASE_TO_REFRESH) {// 处于未刷新状态，更新箭头
                mLoadingListener?.onCancelRefresh()
            }
            if (getVisiableHeight() > mMeasuredHeight) {
                setState(STATE_RELEASE_TO_REFRESH)
            } else {
                setState(STATE_NORMAL)
            }
        }
    }

    override fun releaseAction(): Boolean {
        var isOnRefresh = false
        var height = getVisiableHeight()
        if (height == 0) // 不可见
            isOnRefresh = false
        if (getVisiableHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
            setState(STATE_REFRESHING)
            isOnRefresh = true
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mState == STATE_REFRESHING && height <= mMeasuredHeight) {
        }
        var destHeight = 0 // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mState == STATE_REFRESHING) {
            destHeight = mMeasuredHeight
        }
        smoothScrollTo(destHeight, null)
        return isOnRefresh
    }

    override fun refreshComplete() {
        last_refresh_time.text = refreshTime(Date())
        setState(STATE_DONE)
        Handler().postDelayed({ reset() }, 500)
    }

    fun forceRefresh() {
        setState(STATE_REFRESHING)
        setVisiableHeight(mMeasuredHeight)
    }

    private fun reset() {
        smoothScrollTo(0, object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animator: Animator?) {
            }

            override fun onAnimationEnd(animator: Animator?) {
                setState(STATE_NORMAL)
            }

            override fun onAnimationCancel(animator: Animator?) {
            }

            override fun onAnimationStart(animator: Animator?) {
            }

        })
    }

    private fun smoothScrollTo(destHeight: Int, animatorListener: Animator.AnimatorListener?) {
        var animator = ValueAnimator.ofInt(getVisiableHeight(), destHeight)
        animator.setDuration(300).start()
        animator.addUpdateListener {
            setVisiableHeight(animator.animatedValue as Int)
        }
        if (animatorListener != null) {
            animator.addListener(animatorListener)
        }
        animator.start()
    }

    private fun setVisiableHeight(height: Int) {
        var lp = mContainer?.layoutParams
        lp?.height = if (height < 0) 0 else height
        mContainer?.layoutParams = lp
    }

    fun getVisiableHeight(): Int {
        var height = mContainer?.layoutParams?.height ?: 0
        return height
    }

    fun refreshTime(time: Date): String {
        //获取time距离当前的秒数
        val ct = ((System.currentTimeMillis() - time.time) / 1000).toInt()

        if (ct == 0) {
            return "刚刚"
        }

        if (ct in 1..59) {
            return ct.toString() + "秒前"
        }

        if (ct in 60..3599) {
            return Math.max(ct / 60, 1).toString() + "分钟前"
        }
        if (ct in 3600..86399)
            return (ct / 3600).toString() + "小时前"
        if (ct in 86400..2591999) { //86400 * 30
            val day = ct / 86400
            return day.toString() + "天前"
        }
        return if (ct in 2592000..31103999) { //86400 * 30
            (ct / 2592000).toString() + "月前"
        } else (ct / 31104000).toString() + "年前"
    }

}
