package com.vortexwolf.chan.services.presentation;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.vortexwolf.chan.interfaces.IBusyAdapter;

public class ListViewScrollListener implements ListView.OnScrollListener {

    private final IBusyAdapter mBusyAdapter;

    public ListViewScrollListener(IBusyAdapter busyAdapter) {
        this.mBusyAdapter = busyAdapter;
    }

    @Override
    public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                this.mBusyAdapter.setBusy(false, view);
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                this.mBusyAdapter.setBusy(true, view);
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                this.mBusyAdapter.setBusy(true, view);
                break;
        }
    }
}
