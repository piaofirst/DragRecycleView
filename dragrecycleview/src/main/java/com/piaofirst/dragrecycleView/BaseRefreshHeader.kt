package com.piaofirst.dragrecycleView

/**
 * Created by gjc on 2017/11/6.
 */
interface BaseRefreshHeader {
    fun onMove(delta: Float, mLoadingListener: DragRecycleView.LoadingListener?)
    fun releaseAction(): Boolean
    fun refreshComplete()

    companion object {
        const val STATE_NORMAL = 0
        const val STATE_RELEASE_TO_REFRESH = 1
        const val STATE_REFRESHING = 2
        const val STATE_DONE = 3
    }
}
