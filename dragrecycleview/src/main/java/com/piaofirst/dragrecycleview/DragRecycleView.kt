package com.piaofirst.dragrecycleview

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import java.util.*

/**
 * Created by gic on 2017/11/6.
 */
class DragRecycleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : RecyclerView(context, attrs, defStyle) {

    private var isLoadingData = false
    private var isnomore = false
    private var mRefreshProgressStyle = ProgressStyle.SysProgress
    private var mLoadingMoreProgressStyle = ProgressStyle.SysProgress
    private val mHeaderViews = ArrayList<View>()
    private val mFootViews = ArrayList<View>()
    private var mWrapAdapter: RecyclerView.Adapter<*>? = null
    private var mLastY = -1f
    private var mLoadingListener: LoadingListener? = null
    private var mRefreshHeader: RefreshHeader? = null
    private var pullRefreshEnabled = true
    private var loadingMoreEnabled = true
    private var previousTotal = 0

    companion object {
        private val DRAG_RATE = 2.5f
        private val TYPE_REFRESH_HEADER = -5
        private val TYPE_HEADER = -4
        private val TYPE_NORMAL = 0
        private val TYPE_FOOTER = -3
    }

    private val isOnTop: Boolean
        get() {
            if (mHeaderViews == null || mHeaderViews.isEmpty()) {
                return false
            }

            val view = mHeaderViews[0]
            return view.parent != null
        }

    private val mDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            mWrapAdapter!!.notifyDataSetChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            mWrapAdapter!!.notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            mWrapAdapter!!.notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            mWrapAdapter!!.notifyItemRangeChanged(positionStart, itemCount, payload)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            mWrapAdapter!!.notifyItemRangeRemoved(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            mWrapAdapter!!.notifyItemMoved(fromPosition, toPosition)
        }
    }

    init {
        if (pullRefreshEnabled) {
            var refreshHeader = RefreshHeader(context)
            mHeaderViews.add(0, refreshHeader)
            mRefreshHeader = refreshHeader
            mRefreshHeader!!.setProgressStyle(ProgressStyle.SysProgress)
        }
        var footView = LoadingMoreFooter(context)
        footView.setProgressStyle(mLoadingMoreProgressStyle)
        addFootView(footView)
        mFootViews[0].visibility = View.GONE
    }

    public fun addHeaderView(view: View) {
        if (pullRefreshEnabled && mHeaderViews[0] !is RefreshHeader) {
            var refreshHeader = RefreshHeader(context)
            mHeaderViews.add(0, refreshHeader)
            mRefreshHeader = refreshHeader
            mRefreshHeader!!.setProgressStyle(mRefreshProgressStyle)
        }
        mHeaderViews.add(view)
    }

    public fun addFootView(view: View) {
        mFootViews.clear()
        mFootViews.add(view)
        (view as? LoadingMoreFooter)?.setOnClickListener(OnClickListener {
            if (isLoadingData) return@OnClickListener
            val loadingMoreFooter = view as LoadingMoreFooter
            isLoadingData = true
            loadingMoreFooter.setState(LoadingMoreFooter.STATE_LAODING)
            if (mLoadingListener != null) mLoadingListener!!.onLoadMore()
        })
    }

    fun loadMoreComplete() {
        isLoadingData = false
        var footView = mFootViews[0]
        var itemCount = adapter.itemCount - mHeaderViews.size - mFootViews.size
        if (previousTotal < itemCount) {
            if (footView is LoadingMoreFooter) {
                footView.setState(LoadingMoreFooter.STATE_COMPLETE)
            } else {
                footView.visibility = View.GONE
            }
        } else {
            if (footView is LoadingMoreFooter) {
                footView.setState(LoadingMoreFooter.STATE_NOMORE)
            } else {
                footView.visibility = View.GONE
            }
            isnomore = true
        }
        previousTotal = itemCount
    }

    fun noMoreLoading() {
        isLoadingData = false
        var footView = mFootViews[0]
        isnomore = true
        if (footView is LoadingMoreFooter) {
            footView.setState(LoadingMoreFooter.STATE_NOMORE)
        } else {
            footView.visibility = View.GONE
        }
    }

    fun refreshComplete() {
        mRefreshHeader!!.refreshComplete()
        previousTotal = adapter.itemCount - mHeaderViews.size - mFootViews.size
    }

    fun setRefreshHeader(refreshHeader: RefreshHeader) {
        mRefreshHeader = refreshHeader
    }

    fun setPullRefreshEnabled(enabled: Boolean) {
        pullRefreshEnabled = enabled
    }

    fun setLoadingMoreEnabled(enabled: Boolean) {
        loadingMoreEnabled = enabled
        if (!enabled) {
            if (mFootViews.size > 0) {
                mFootViews[0].visibility = View.GONE
            }
        }
    }

    fun setRefreshProgressStyle(style: String) {
        mRefreshProgressStyle = style
        if (mRefreshHeader != null) {
            mRefreshHeader!!.setProgressStyle(style)
        }
    }

    fun setLaodingMoreProgressStyle(style: String) {
        mLoadingMoreProgressStyle = style
        if (mFootViews.size > 0 && mFootViews[0] is LoadingMoreFooter) {
            (mFootViews[0] as LoadingMoreFooter).setProgressStyle(style)
        }
    }

    fun setHeaderArrow(resid: Int) {
        if (mRefreshHeader != null) {
            mRefreshHeader!!.setHeaderArrow(resid)
        }
    }

    fun setLoadingListener(listener: LoadingListener) {
        mLoadingListener = listener
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        mWrapAdapter = WrapAdapter(mHeaderViews, mFootViews, adapter)
        super.setAdapter(mWrapAdapter)
        adapter.registerAdapterDataObserver(mDataObserver)
        previousTotal = adapter.itemCount
    }

    fun getHeaderViews(): ArrayList<View> {
        return mHeaderViews
    }

    fun getFootViews(): ArrayList<View> {
        return mFootViews
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData && loadingMoreEnabled) {
            val layoutManager = layoutManager
            val lastVisibleItemPosition: Int
            if (layoutManager is GridLayoutManager) {
                lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            } else if (layoutManager is StaggeredGridLayoutManager) {
                val into = IntArray(layoutManager.spanCount)
                layoutManager.findLastVisibleItemPositions(into)
                lastVisibleItemPosition = findMax(into)
            } else {
                lastVisibleItemPosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }
            if (layoutManager.childCount > 0
                    && lastVisibleItemPosition >= layoutManager.itemCount - 1
                    && layoutManager.itemCount > layoutManager.childCount
                    && !isnomore && mRefreshHeader!!.getState() < BaseRefreshHeader.STATE_REFRESHING) {
                val footView = mFootViews[0]
                isLoadingData = true
                if (footView is LoadingMoreFooter) {
                    footView.setState(LoadingMoreFooter.STATE_LAODING)
                } else {
                    footView.visibility = View.VISIBLE
                }
                mLoadingListener!!.onLoadMore()
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (mLastY == -1f) {
            mLastY = ev.rawY
        }
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastY = ev.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.rawY - mLastY
                mLastY = ev.rawY
                if (isOnTop && pullRefreshEnabled) {
                    mRefreshHeader!!.onMove(if (deltaY > 0) deltaY / DRAG_RATE else deltaY, mLoadingListener)
                    if (mRefreshHeader!!.getVisiableHeight() > 0
                            && mRefreshHeader!!.getState() < BaseRefreshHeader.STATE_REFRESHING) {
                        return false
                    }
                }
            }
            else -> {
                mLastY = -1f // 还原
                if (isOnTop && pullRefreshEnabled) {
                    if (mRefreshHeader!!.releaseAction()) {
                        if (mLoadingListener != null) {
                            mLoadingListener!!.onRefresh()
                            isnomore = false
                            previousTotal = 0
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    fun forceRefresh() {
        mRefreshHeader!!.forceRefresh()
        if (mLoadingListener != null) {
            mLoadingListener!!.onRefresh()
            isnomore = false
            previousTotal = 0
        }
    }

    private fun findMax(lastPositions: IntArray): Int {
        return lastPositions.max()
                ?: lastPositions[0]
    }

    private fun findMin(firstPositions: IntArray): Int {
        return firstPositions.min()
                ?: firstPositions[0]
    }

    private inner class WrapAdapter(private val mHeaderViews: ArrayList<View>, private val mFootViews: ArrayList<View>,
                                    private val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var headerPosition = 1

        val headersCount: Int
            get() = mHeaderViews.size

        val footersCount: Int
            get() = mFootViews.size

        /**
         * 对GridLayoutManager的处理；该方法会在RecyclerView.setAdapter()方法中被调用，
         * 因此前面建议保证一定在setLayoutManager方法之后调用该方法
         * @param recyclerView
         */
        override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
            super.onAttachedToRecyclerView(recyclerView)
            val manager = recyclerView!!.layoutManager
            if (manager is GridLayoutManager) {
                manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (isHeader(position) || isFooter(position))
                            manager.spanCount
                        else
                            1
                    }
                }
            }
        }

        /**
         * 对StaggeredGridLayoutManager的处理
         * @param holder
         */
        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder?) {
            super.onViewAttachedToWindow(holder)
            val lp = holder!!.itemView.layoutParams
            if (lp != null
                    && lp is StaggeredGridLayoutManager.LayoutParams
                    && (isHeader(holder.layoutPosition) || isFooter(holder.layoutPosition))) {
                lp.isFullSpan = true
            }
        }

        fun isHeader(position: Int): Boolean {
            return position >= 0 && position < mHeaderViews.size
        }

        fun isFooter(position: Int): Boolean {
            return position < itemCount && position >= itemCount - mFootViews.size
        }

        fun isRefreshHeader(position: Int): Boolean {
            return position == 0
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (isHeader(position)) {
                return
            }
            val adjPosition = position - headersCount
            val adapterCount: Int
            if (adapter != null) {
                adapterCount = adapter.itemCount
                if (adjPosition < adapterCount) {
                    adapter.onBindViewHolder(holder, adjPosition)
                    return
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                TYPE_REFRESH_HEADER -> SimpleViewHolder(mHeaderViews[0])
                TYPE_HEADER -> SimpleViewHolder(mHeaderViews[headerPosition++])
                TYPE_FOOTER -> SimpleViewHolder(mFootViews[0])
                else -> adapter!!.onCreateViewHolder(parent, viewType)
            }
        }

        override fun getItemCount(): Int {
            return if (adapter != null) {
                headersCount + footersCount + adapter.itemCount
            } else {
                headersCount + footersCount
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER
            }
            if (isHeader(position)) {
                return TYPE_HEADER
            }
            if (isFooter(position)) {
                return TYPE_FOOTER
            }
            val adjPosition = position - headersCount

            val adapterCount: Int
            if (adapter != null) {
                adapterCount = adapter.itemCount
                if (adjPosition < adapterCount) {
                    return adapter.getItemViewType(adjPosition)
                }
            }
            return TYPE_NORMAL
        }

        override fun getItemId(position: Int): Long {
            if (adapter != null && position >= headersCount) {
                val adjPosition = position - headersCount
                val adapterCount = adapter.itemCount
                if (adjPosition < adapterCount) {
                    return adapter.getItemId(adjPosition)
                }
            }
            return -1
        }

        override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
            adapter?.unregisterAdapterDataObserver(observer)
        }

        override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
            adapter?.registerAdapterDataObserver(observer)
        }

        private inner class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }

    interface LoadingListener {
        fun onRefresh()
        fun onLoadMore()
        fun onCancelRefresh()
    }
}
