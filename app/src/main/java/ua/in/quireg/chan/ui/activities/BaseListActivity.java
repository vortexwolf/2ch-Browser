package ua.in.quireg.chan.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.asynctasks.CheckCloudflareTask;
import ua.in.quireg.chan.asynctasks.DisplayImageUriTask;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.interfaces.ICheckCaptchaView;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.settings.SettingsEntity;

public abstract class BaseListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private enum ViewType {
        LIST, LOADING, ERROR, CAPTCHA
    }

    private View mLoadingView = null;
    protected ListView mListView = null;
    private View mErrorView = null;
    private View mCaptchaView = null;
    private SwipeRefreshLayout mRefreshView = null;
    private ViewType mCurrentView = null;
    private Button mCaptchaSendButton;
    private CheckCloudflareTask mCurrentCheckTask = null;
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private SettingsEntity settingsEntity = mSettings.getCurrentSettings();

    protected boolean mVisible = false;

    @Override
    public void onRefresh() {
        BaseListActivity.this.refresh();
    }

    protected void hideRefreshView() {
        mRefreshView.setRefreshing(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mVisible = false;
    }

    @Override
    protected void onResume() {

        if (settingsEntity.compareTo(mSettings.getCurrentSettings()) != 0) {
            //settings has been changed
            settingsEntity = mSettings.getCurrentSettings();
            resetUI();
        }

        super.onResume();
        this.mVisible = true;
    }

    /**
     * Returns the layout resource Id associated with this activity
     */
    protected abstract int getLayoutId();

    protected abstract void refresh();

    protected void setContainerView(@LayoutRes int layoutResID){
        //super.setContainerView(layoutResID);
        mListView = (ListView) findViewById(android.R.id.list);
    }

    protected ListView getListView() {
        return this.mListView;
    }

    protected void setListAdapter(ListAdapter adapter) {
        this.mListView.setAdapter(adapter);
    }

    /**
     * Reloads UI on the page
     */
    protected void resetUI() {
        // setting of the theme goes first
        this.setTheme(mSettings.getTheme());

        // completely reload the root view, get loading and error views
        setContentView(R.layout.base_activity);
        this.mLoadingView = this.findViewById(R.id.loadingView);
        this.mErrorView = this.findViewById(R.id.error);
        this.mCaptchaView = this.findViewById(R.id.captchaView);

        this.mRefreshView = (SwipeRefreshLayout) this.findViewById(R.id.refreshView);
        this.mRefreshView.setOnRefreshListener(this);
        this.mRefreshView.setEnabled(mSettings.isSwipeToRefresh());

        this.switchToView(this.mCurrentView);
    }


    /**
     * Shows the loading indicator
     */
    protected void switchToLoadingView() {
        this.switchToView(ViewType.LOADING);
    }

    /**
     * Shows the normal list
     */
    protected void switchToListView() {
        this.switchToView(ViewType.LIST);
    }

    /**
     * Shows the error message
     */
    protected void switchToErrorView(String message) {
        this.switchToView(ViewType.ERROR);

        TextView errorTextView = (TextView) this.mErrorView.findViewById(R.id.error_text);
        errorTextView.setText(message != null ? message : this.getString(R.string.error_unknown));
    }

    protected void switchToCaptchaView(final IWebsite website, final CaptchaEntity captcha) {
        if (this.mCaptchaView == null) {
            this.switchToErrorView("The captcha view is not designed yet.");
            return;
        }

        this.switchToView(ViewType.CAPTCHA);

        ImageView captchaImage = (ImageView) this.mCaptchaView.findViewById(R.id.cloudflare_captcha_image);
        final EditText captchaAnswer = (EditText) this.mCaptchaView.findViewById(R.id.cloudflare_captcha_input);
        this.mCaptchaSendButton = (Button) this.mCaptchaView.findViewById(R.id.cloudflare_send_button);

        captchaAnswer.setText("");
        captchaAnswer.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

        this.mCaptchaSendButton.setOnClickListener(new View.OnClickListener() {
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
        if (this.mVisible) {
            AppearanceUtils.showLongToast(this, message);
        }
    }

    /**
     * Switches the page between the list view, loading view and error view
     */
    private void switchToView(ViewType vt) {
        this.mCurrentView = vt;

        if (vt == null) {
            return;
        }

        switch (vt) {
            case LIST:
                this.mListView.setVisibility(View.VISIBLE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.GONE);
                this.setCaptchaViewVisibility(View.GONE);
                this.mRefreshView.setRefreshing(false);
                break;
            case LOADING:
                this.mListView.setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.VISIBLE);
                this.mErrorView.setVisibility(View.GONE);
                this.setCaptchaViewVisibility(View.GONE);
                this.mRefreshView.setRefreshing(false);
                break;
            case ERROR:
                this.mListView.setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.VISIBLE);
                this.setCaptchaViewVisibility(View.GONE);
                this.mRefreshView.setRefreshing(false);
                break;
            case CAPTCHA:
                this.mListView.setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.GONE);
                this.setCaptchaViewVisibility(View.VISIBLE);
                this.mRefreshView.setRefreshing(false);
                break;
        }
    }

    private void setCaptchaViewVisibility(int visibility) {
        if (this.mCaptchaView != null) {
            this.mCaptchaView.setVisibility(visibility);
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
            BaseListActivity.this.refresh();
        }

        @Override
        public void showError(String message) {
            message = message != null ? message : "Incorrect captcha.";
            AppearanceUtils.showLongToast(getApplicationContext(), message);

            mCaptchaSendButton.setEnabled(true);
            BaseListActivity.this.refresh();
        }
    }
}
