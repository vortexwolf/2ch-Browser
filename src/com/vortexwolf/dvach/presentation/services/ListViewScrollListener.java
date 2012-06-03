package com.vortexwolf.dvach.presentation.services;

import com.vortexwolf.dvach.interfaces.IBusyAdapter;

import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

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
	        	mBusyAdapter.setBusy(false, view);
	            break;
	        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
	        	mBusyAdapter.setBusy(true, view);
	            break;
	        case OnScrollListener.SCROLL_STATE_FLING:
	        	mBusyAdapter.setBusy(true, view);
	            break;
		}
	}
}
