package com.vortexwolf.dvach.activities;

import java.io.File;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.asynctasks.DownloadFileTask;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.controls.WebViewFixed;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;
import com.vortexwolf.dvach.interfaces.IDownloadFileView;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.Tracker;

import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class BrowserActivity extends Activity {
    public static final String TAG = "BrowserActivity";

    private enum ViewType {
        PAGE, LOADING, ERROR
    }

    private MainApplication mApplication;
    private Tracker mTracker;
    private ICacheDirectoryManager mCacheDirectoryManager;

    private ViewGroup mRootView;
    private WebView mWebView = null;
    private View mLoadingView = null;
    private View mErrorView = null;

    private Uri mUri = null;
    private String mTitle = null;
    private OnCancelListener mCurrentCancelListener;
    
    private Menu mMenu;
    private boolean mImageLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_PROGRESS);

        this.mApplication = (MainApplication) this.getApplication();
        this.mTracker = this.mApplication.getTracker();
        this.mCacheDirectoryManager = this.mApplication.getCacheManager();

        this.resetUI();

        this.mWebView.setInitialScale(100);
        WebSettings settings = mWebView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        this.mUri = this.getIntent().getData();
        this.mTitle = this.mUri.toString();
        this.setTitle(this.mTitle);

        File cachedFile = this.mCacheDirectoryManager.getCachedImageFileForRead(this.mUri);
        if (cachedFile.exists()) {
            // show from cache
            this.setImage(cachedFile);
        } else {
            // download image and cache it
            File writeCachedFile = this.mCacheDirectoryManager.getCachedImageFileForWrite(this.mUri);
            new DownloadFileTask(this, this.mUri, writeCachedFile, new BrowserDownloadFileView(), false).execute();
        }

        this.mTracker.trackActivityView(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.mCurrentCancelListener != null) {
            mCurrentCancelListener.onCancel(null);
        }

        // Must remove the WebView from the view system before destroying.
        this.mRootView.removeView(this.mWebView);
        this.mWebView.removeAllViews();
        this.mWebView.destroy();
    }

    private void resetUI() {
        this.setTheme(this.mApplication.getSettings().getTheme());
        this.setContentView(R.layout.browser);

        this.mRootView = (ViewGroup) this.findViewById(R.id.browser_view);
        this.mWebView = (WebViewFixed) this.findViewById(R.id.webview);
        this.mLoadingView = this.findViewById(R.id.loadingView);
        this.mErrorView = this.findViewById(R.id.error);

        TypedArray a = this.mApplication.getTheme().obtainStyledAttributes(R.styleable.Theme);
        int background = a.getColor(R.styleable.Theme_activityRootBackground, 0);
        this.mWebView.setBackgroundColor(background);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser, menu);
        
        this.mMenu = menu;
        this.updateOptionsMenu();
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.open_browser_menu_id:
                BrowserLauncher.launchExternalBrowser(this, this.mUri.toString());
                break;
            case R.id.save_menu_id:
                new DownloadFileTask(this, this.mUri).execute();
                break;
            case R.id.share_menu_id:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_STREAM, this.mUri);
                this.startActivity(Intent.createChooser(i, this.getString(R.string.share_via)));
                break;
        }

        return true;
    }

    @Override
    public File getCacheDir() {
        return this.getApplicationContext().getCacheDir();
    }

    private void setImage(File file) {
        this.mWebView.loadUrl(Uri.fromFile(file).toString());
        
        this.mImageLoaded = true;
        this.updateOptionsMenu();
    }
    
    private void updateOptionsMenu() {
        if(this.mMenu == null) return;
        
        MenuItem saveMenuItem = this.mMenu.findItem(R.id.save_menu_id);
        MenuItem shareMenuItem = this.mMenu.findItem(R.id.share_menu_id);
        
        saveMenuItem.setVisible(this.mImageLoaded);
        shareMenuItem.setVisible(this.mImageLoaded);
    }

    private void switchToLoadingView() {
        this.switchToView(ViewType.LOADING);
    }

    private void switchToPageView() {
        this.switchToView(ViewType.PAGE);
    }

    private void switchToErrorView(String message) {
        this.switchToView(ViewType.ERROR);

        TextView errorTextView = (TextView) this.mErrorView.findViewById(R.id.error_text);
        errorTextView.setText(message != null
                ? message
                : this.getString(R.string.error_unknown));
    }

    private void switchToView(ViewType vt) {
        if (vt == null) return;

        switch (vt) {
            case PAGE:
                this.mWebView.setVisibility(View.VISIBLE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.GONE);
                break;
            case LOADING:
                this.mWebView.setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.VISIBLE);
                this.mErrorView.setVisibility(View.GONE);
                break;
            case ERROR:
                this.mWebView.setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private class BrowserDownloadFileView implements IDownloadFileView {

        private double mMaxValue = -1;

        @Override
        public void setProgress(int value) {
            if (mMaxValue > 0) {
                double percent = value / mMaxValue;
                BrowserActivity.this.setProgress((int) (percent * Window.PROGRESS_END)); // from 0 to 10000
            } else {
                BrowserActivity.this.setProgress(Window.PROGRESS_INDETERMINATE_ON);
            }
        }

        @Override
        public void setMax(int value) {
            this.mMaxValue = (double) value;
        }

        @Override
        public void showLoading(String message) {
            BrowserActivity.this.switchToLoadingView();
        }

        @Override
        public void hideLoading() {
            BrowserActivity.this.setProgress(Window.PROGRESS_END);
            BrowserActivity.this.switchToPageView();
        }

        @Override
        public void setOnCancelListener(OnCancelListener listener) {
            BrowserActivity.this.mCurrentCancelListener = listener;
        }

        @Override
        public void showSuccess(File file) {
            BrowserActivity.this.setImage(file);
        }

        @Override
        public void showError(String error) {
            BrowserActivity.this.switchToErrorView(error);
        }

        @Override
        public void showFileExists(File file) {
            // it shouldn't be called, because I checked this situation in the
            // onCreate method
            BrowserActivity.this.setImage(file);
        }

    }

}
