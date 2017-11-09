package com.piaofirst.dragrecycleview

/**
 * Created by gjc on 2017/11/6.
 */
interface BaseRefreshHeader {
    abstract fun onMove(delta: Float, mLoadingListener: DragRecycleView.LoadingListener?)
    abstract fun releaseAction(): Boolean
    abstract fun refreshComplete()

    companion object {
        const val STATE_NORMAL = 0
        const val STATE_RELEASE_TO_REFRESH = 1
        const val STATE_REFRESHING = 2
        const val STATE_DONE = 3
    }
}
