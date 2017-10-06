package ua.in.quireg.chan.ui.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.asynctasks.CheckCloudflareTask;
import ua.in.quireg.chan.asynctasks.DisplayImageUriTask;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.interfaces.ICheckCaptchaView;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.settings.ApplicationSettings;

public abstract class BaseListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private enum ViewType {
        LIST, LOADING, ERROR, CAPTCHA
    }

    private View mLoadingView;
    private View mErrorView;
    private View mCaptchaView;
    private SwipeRefreshLayout mRefreshView;

    private Button mCaptchaSendButton;
    private CheckCloudflareTask mCurrentCheckTask = null;
    protected IWebsite mWebsite = Websites.getDefault();
    private ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);

    protected ListView mListView;

    protected boolean mVisible = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mListView = (ListView) view.findViewById(android.R.id.list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mListView.setNestedScrollingEnabled(true);
        }

        //Slow down the list scroll for better experience.
        //mListView.setFriction(ViewConfiguration.getScrollFriction() * 2);

        mLoadingView = view.findViewById(R.id.loadingView);
        mErrorView = view.findViewById(R.id.error);
        mCaptchaView = view.findViewById(R.id.captchaView);
        mRefreshView = (SwipeRefreshLayout) view.findViewById(R.id.refreshView);

        if (mRefreshView != null && mSettings.isSwipeToRefresh()) {
            mRefreshView.setOnRefreshListener(this);
            mRefreshView.setEnabled(mSettings.isSwipeToRefresh());
        }
    }

    public void setTitle(String pageTitle) {
        ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(pageTitle);
        }
    }

    protected void hideRefreshView() {
        if(mRefreshView != null){
            mRefreshView.setRefreshing(false);
        }
    }
    /**
     * Shows the loading indicator
     */
    protected void switchToLoadingView() {
        switchToView(ViewType.LOADING);
    }

    /**
     * Shows the normal list
     */
    protected void switchToListView() {
        switchToView(ViewType.LIST);
    }

    /**
     * Shows the error message
     */
    protected void switchToErrorView(String message) {
        switchToView(ViewType.ERROR);

        TextView errorTextView = (TextView) mErrorView.findViewById(R.id.error_text);
        errorTextView.setText(message != null ? message : getString(R.string.error_unknown));
    }

    protected void switchToCaptchaView(final IWebsite website, final CaptchaEntity captcha) {
        if (mCaptchaView == null) {
            switchToErrorView("The captcha view is not designed yet.");
            return;
        }

        switchToView(ViewType.CAPTCHA);

        ImageView captchaImage = (ImageView) mCaptchaView.findViewById(R.id.cloudflare_captcha_image);
        final EditText captchaAnswer = (EditText) mCaptchaView.findViewById(R.id.cloudflare_captcha_input);
        mCaptchaSendButton = (Button) mCaptchaView.findViewById(R.id.cloudflare_send_button);

        captchaAnswer.setText("");
        captchaAnswer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(captchaAnswer.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    if (mCurrentCheckTask != null) {
                        mCurrentCheckTask.cancel(true);
                    }

                    mCurrentCheckTask = new CheckCloudflareTask(website, captcha, captchaAnswer.getText().toString(), new CheckCaptchaView());
                    mCurrentCheckTask.execute();
                    return true;
                }
                return false;
            }
        });

        mCaptchaSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentCheckTask != null) {
                    mCurrentCheckTask.cancel(true);
                }

                mCurrentCheckTask = new CheckCloudflareTask(website, captcha, captchaAnswer.getText().toString(), new CheckCaptchaView());
                mCurrentCheckTask.execute();
            }
        });

        // display captcha image
        DisplayImageUriTask task = new DisplayImageUriTask(captcha.getUrl(), captchaImage);
        task.execute();
    }

    protected void showToastIfVisible(String message) {
        if (mVisible) {
            AppearanceUtils.showToastMessage(getContext(), message);
        }
    }

    /**
     * Switches the page between the list view, loading view and error view
     */
    private void switchToView(ViewType vt) {

        switch (vt) {
            case LIST:
                setListViewVisibility(View.VISIBLE);
                setLoadingViewVisibility(View.GONE);
                setErrorViewVisibility(View.GONE);
                setCaptchaViewVisibility(View.GONE);
                setRefreshViewState(false);
                break;
            case LOADING:
                setListViewVisibility(View.GONE);
                setLoadingViewVisibility(View.VISIBLE);
                setErrorViewVisibility(View.GONE);
                setCaptchaViewVisibility(View.GONE);
                setRefreshViewState(false);
                break;
            case ERROR:
                setListViewVisibility(View.GONE);
                setLoadingViewVisibility(View.GONE);
                setErrorViewVisibility(View.VISIBLE);
                setCaptchaViewVisibility(View.GONE);
                setRefreshViewState(false);
                break;
            case CAPTCHA:
                setListViewVisibility(View.GONE);
                setLoadingViewVisibility(View.GONE);
                setErrorViewVisibility(View.GONE);
                setCaptchaViewVisibility(View.VISIBLE);
                setRefreshViewState(false);
                break;
            default:
                break;
        }
    }

    private void setCaptchaViewVisibility(int visibility) {
        if (mCaptchaView != null) {
            mCaptchaView.setVisibility(visibility);
        }
    }

    private void setListViewVisibility(int visibility) {
        if (mListView != null) {
            mListView.setVisibility(visibility);
        }
    }

    private void setLoadingViewVisibility(int visibility) {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(visibility);
        }
    }

    private void setErrorViewVisibility(int visibility) {
        if (mErrorView != null) {
            mErrorView.setVisibility(visibility);
        }
    }

    private void setRefreshViewState(boolean state) {
        if (mRefreshView != null) {
            mRefreshView.setRefreshing(state);
        }
    }

    private class CheckCaptchaView implements ICheckCaptchaView {
        @Override
        public void beforeCheck() {
            mCaptchaSendButton.setEnabled(false);
        }

        @Override
        public void showSuccess() {
            mCaptchaSendButton.setEnabled(true);
        }

        @Override
        public void showError(String message) {
            message = message != null ? message : "Incorrect captcha.";
            AppearanceUtils.showToastMessage(getContext(), message);

            mCaptchaSendButton.setEnabled(true);
        }
    }
}
