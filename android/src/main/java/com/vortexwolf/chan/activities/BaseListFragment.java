package com.vortexwolf.chan.activities;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.library.MyLog;

public abstract class BaseListFragment extends ListFragment {
    private enum ViewType {
        LIST, LOADING, ERROR
    };

    private View mLoadingView = null;
    private View mErrorView = null;
    private ViewType mCurrentView = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mLoadingView = view.findViewById(R.id.loadingView);
        this.mErrorView = view.findViewById(R.id.error);
        this.switchToView(this.mCurrentView);
    }

    /** Shows the loading indicator */
    protected void switchToLoadingView() {
        this.switchToView(ViewType.LOADING);
    }

    /** Shows the normal list */
    protected void switchToListView() {
        this.switchToView(ViewType.LIST);
    }

    /** Shows the error message */
    protected void switchToErrorView(String message) {
        this.switchToView(ViewType.ERROR);

        TextView errorTextView = (TextView) this.mErrorView.findViewById(R.id.error_text);
        errorTextView.setText(message != null ? message : this.getString(R.string.error_unknown));
    }

    /** Switches the page between the list view, loading view and error view */
    private void switchToView(ViewType vt) {
        this.mCurrentView = vt;

        if (vt == null) {
            return;
        }

        ListView listView;
        try {
            listView = this.getListView();
        } catch (IllegalStateException e) {
            MyLog.e("BaseListFragment", e);
            return;
        }

        switch (vt) {
            case LIST:
                listView.setVisibility(View.VISIBLE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.GONE);
                break;
            case LOADING:
                listView.setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.VISIBLE);
                this.mErrorView.setVisibility(View.GONE);
                break;
            case ERROR:
                listView.setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.VISIBLE);
                break;
        }
    }
}
