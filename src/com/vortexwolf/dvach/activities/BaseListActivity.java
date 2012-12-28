package com.vortexwolf.dvach.activities;

import java.lang.reflect.Field;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;

import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseListActivity extends ListActivity {
    private enum ViewType {
        LIST, LOADING, ERROR
    };

    private View mLoadingView = null;
    private View mErrorView = null;
    private ViewType mCurrentView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_PROGRESS);
    }

    /** Returns the layout resource Id associated with this activity */
    protected abstract int getLayoutId();

    /** Reloads UI on the page */
    protected void resetUI() {
        // setting of the theme goes first
        this.setTheme(this.getMainApplication().getSettings().getTheme());

        // memorize the current position of the list before it is reloaded
        AppearanceUtils.ListViewPosition position = AppearanceUtils.getCurrentListPosition(this.getListView());

        // completely reload the root view, get loading and error views
        this.setContentView(this.getLayoutId());
        mLoadingView = this.findViewById(R.id.loadingView);
        mErrorView = this.findViewById(R.id.error);

        this.switchToView(mCurrentView);

        // restore the position of the list
        this.getListView().setSelectionFromTop(position.position, position.top);
    }

    /** Returns the main class of the application */
    protected MainApplication getMainApplication() {
        return (MainApplication) super.getApplication();
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

        TextView errorTextView = (TextView) mErrorView.findViewById(R.id.error_text);
        errorTextView.setText(message != null
                ? message
                : this.getString(R.string.error_unknown));
    }

    /** Switches the page between the list view, loading view and error view */
    private void switchToView(ViewType vt) {
        this.mCurrentView = vt;

        if (vt == null) return;

        switch (vt) {
            case LIST:
                this.getListView().setVisibility(View.VISIBLE);
                mLoadingView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.GONE);
                break;
            case LOADING:
                this.getListView().setVisibility(View.GONE);
                mLoadingView.setVisibility(View.VISIBLE);
                mErrorView.setVisibility(View.GONE);
                break;
            case ERROR:
                this.getListView().setVisibility(View.GONE);
                mLoadingView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
                break;
        }
    }
}
