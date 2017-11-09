package com.piaofirst.dragrecycleview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class LinearActivity extends AppCompatActivity {
    private DragRecycleView mRecyclerView;
    private MyAdapter mAdapter;
    private ArrayList<String> listData;
    private int refreshTime = 0;
    private int times = 0;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);
        mRecyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoaderIndicator);
//        mRecyclerView.setRefreshProgressStyle(ProgressStyle.SysProgress);
        mRecyclerView.setLaodingMoreProgressStyle(ProgressStyle.BallRotateIndicator);
        mRecyclerView.setHeaderArrow(R.drawable.iconfont_downgrey);

        View header = LayoutInflater.from(this).inflate(R.layout.recyclerview_header, (ViewGroup) findViewById(android.R.id.content), false);
        mRecyclerView.addHeaderView(header);

        handler = new Handler();

        mRecyclerView.setLoadingListener(new DragRecycleView.LoadingListener() {
            @Override
            public void onRefresh() {
                refreshTime++;
                times = 0;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listData.clear();
                        for (int i = 0; i < 15; i++) {
                            listData.add("item" + i + "after " + refreshTime + " times of refresh");
                        }
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.refreshComplete();
                    }
                }, 1000);            //refresh data here
            }

            @Override
            public void onLoadMore() {
                if (times < 2) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int preSize = mAdapter.getItemCount() + mRecyclerView.getHeaderViews().size();
                            for (int i = 0; i < 15; i++) {
                                listData.add("item" + (i + listData.size()));
                            }
                            mAdapter.notifyItemRangeInserted(preSize, 15);
                            mRecyclerView.loadMoreComplete();
                        }
                    }, 1000);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                            mRecyclerView.loadMoreComplete();
                        }
                    }, 1000);
                }
                times++;
            }

            @Override
            public void onCancelRefresh() {
                handler.removeCallbacksAndMessages(null);
            }
        });

        listData = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            listData.add("item" + (i + listData.size()));
        }
        mAdapter = new MyAdapter(listData);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.forceRefresh();
    }


}
