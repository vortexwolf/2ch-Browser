package com.vortexwolf.chan.activities;

import java.io.File;

import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.asynctasks.DownloadFileTask;
import com.vortexwolf.chan.boards.dvach.DvachUriParser;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.MainApplication;
import com.vortexwolf.chan.common.controls.WebViewFixed;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.interfaces.ICacheDirectoryManager;
import com.vortexwolf.chan.interfaces.IDownloadFileView;
import com.vortexwolf.chan.services.BrowserLauncher;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class BrowserActivity extends Activity {
    public static final String TAG = "BrowserActivity";

    private enum ViewType {
        PAGE, LOADING, ERROR
    }

    private final MyTracker mTracker = Factory.resolve(MyTracker.class);
    private final ICacheDirectoryManager mCacheDirectoryManager = Factory.resolve(ICacheDirectoryManager.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private final DvachUriParser mUriParser = Factory.resolve(DvachUriParser.class);

    private WebView mWebView = null;
    private View mLoadingView = null;
    private View mErrorView = null;

    private Uri mUri = null;
    private String mTitle = null;

    private Menu mMenu;
    private boolean mImageLoaded = false;
    private File mLoadedFile = null;
    private ViewType mViewType = null;

    private DownloadFileTask mCurrentTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_PROGRESS);

        this.setTheme(mSettings.getTheme());
        this.setContentView(R.layout.browser);

        this.mWebView = (WebViewFixed) this.findViewById(R.id.webview);
        this.mLoadingView = this.findViewById(R.id.loading);
        this.mErrorView = this.findViewById(R.id.error);

        int background = AppearanceUtils.getThemeColor(this.getTheme(), R.styleable.Theme_activityRootBackground);
        AppearanceUtils.prepareWebView(this.mWebView, background, mSettings.isDisplayZoomControls());

        this.mUri = this.getIntent().getData();
        this.mTitle = this.mUri.toString();
        this.setTitle(this.mTitle);

        this.loadImage();

        this.mTracker.setBoardVar(mUriParser.getBoardName(this.mUri));
        this.mTracker.trackActivityView(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.mCurrentTask != null) {
            this.mCurrentTask.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
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
            case R.id.refresh_menu_id:
                this.loadImage();
                break;
            case R.id.share_menu_id:
                Intent shareImageIntent = new Intent(Intent.ACTION_SEND);
                shareImageIntent.setType("image/jpeg");
                shareImageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(this.mLoadedFile));
                this.startActivity(Intent.createChooser(shareImageIntent, this.getString(R.string.share_via)));
                break;
            case R.id.share_link_menu_id:
                Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                shareLinkIntent.setType("text/plain");
                shareLinkIntent.putExtra(Intent.EXTRA_SUBJECT, this.mUri.toString());
                shareLinkIntent.putExtra(Intent.EXTRA_TEXT, this.mUri.toString());
                this.startActivity(Intent.createChooser(shareLinkIntent, this.getString(R.string.share_via)));
                break;
            case R.id.menu_search_tineye_id:
                String tineyeSearchUrl = "http://www.tineye.com/search?url=" + this.mUri;
                BrowserLauncher.launchExternalBrowser(this.getApplicationContext(), tineyeSearchUrl);
                break;
            case R.id.menu_search_google_id:
                String googleSearchUrl = "http://www.google.com/searchbyimage?image_url=" + this.mUri;
                BrowserLauncher.launchExternalBrowser(this.getApplicationContext(), googleSearchUrl);
                break;
            case R.id.menu_image_operations_id:
                String imageOpsUrl = "http://imgops.com/" + this.mUri;
                BrowserLauncher.launchExternalBrowser(this.getApplicationContext(), imageOpsUrl);
                break;
        }

        return true;
    }

    private void loadImage() {
        if (this.mCurrentTask != null) {
            this.mCurrentTask.cancel(true);
        }

        File cachedFile = this.mCacheDirectoryManager.getCachedImageFileForRead(this.mUri);
        if (cachedFile.exists()) {
            // show from cache
            this.setImage(cachedFile);
        } else {
            // download image and cache it
            File writeCachedFile = this.mCacheDirectoryManager.getCachedImageFileForWrite(this.mUri);
            this.mCurrentTask = new DownloadFileTask(this, this.mUri, writeCachedFile, new BrowserDownloadFileView(), false);
            this.mCurrentTask.execute();
        }
    }

    private void setImage(File file) {
        this.mLoadedFile = file;

        AppearanceUtils.setScaleWebView(this.mWebView, file, this);
        this.mWebView.loadUrl(Uri.fromFile(file).toString());

        this.mImageLoaded = true;
        this.updateOptionsMenu();
    }

    private void updateOptionsMenu() {
        if (this.mMenu == null) {
            return;
        }

        MenuItem saveMenuItem = this.mMenu.findItem(R.id.save_menu_id);
        MenuItem shareMenuItem = this.mMenu.findItem(R.id.share_menu_id);
        MenuItem refreshMenuItem = this.mMenu.findItem(R.id.refresh_menu_id);

        saveMenuItem.setVisible(this.mImageLoaded);
        shareMenuItem.setVisible(this.mImageLoaded);
        refreshMenuItem.setVisible(this.mViewType == ViewType.ERROR);
    }

    private void switchToLoadingView() {
        this.switchToView(ViewType.LOADING);
    }

    private void switchToPageView() {
        this.switchToView(ViewType.PAGE);
    }

    private void switchToErrorView(String message) {
        this.switchToView(ViewType.ERROR);
        this.updateOptionsMenu();

        TextView errorTextView = (TextView) this.mErrorView.findViewById(R.id.error_text);
        errorTextView.setText(message != null ? message : this.getString(R.string.error_unknown));
    }

    private void switchToView(ViewType vt) {
        this.mViewType = vt;
        if (vt == null) {
            return;
        }

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
        public void setCurrentProgress(int value) {
            if (this.mMaxValue > 0) {
                double percent = value / this.mMaxValue;
                BrowserActivity.this.setProgress((int) (percent * Window.PROGRESS_END)); // from 0 to 10000
            } else {
                BrowserActivity.this.setProgress(Window.PROGRESS_INDETERMINATE_ON);
            }
        }

        @Override
        public void setMaxProgress(int value) {
            this.mMaxValue = value;
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
