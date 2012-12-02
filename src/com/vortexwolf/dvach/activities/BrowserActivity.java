package com.vortexwolf.dvach.activities;
import java.io.File;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.controls.WebViewFixed;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.Tracker;
import com.vortexwolf.dvach.services.domain.SaveFileService;

import android.app.Activity;
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
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowserActivity extends Activity {
	public static final String TAG = "BrowserActivity";
	
	private enum ViewType { PAGE, LOADING }
	
    private MainApplication mApplication;
	private SaveFileService mDownloadFileService;
	private Tracker mTracker;
	
	private WebView mWebView;
	private View mLoadingView = null;
	
	private Uri mUri = null;
	private String mTitle = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
        
        this.mApplication = (MainApplication) this.getApplication();
        this.mDownloadFileService = this.mApplication.getSaveFileService();
        this.mTracker = this.mApplication.getTracker();
        
        this.mTracker.trackActivityView(TAG);
        
        this.resetUI();
        
		WebSettings settings = mWebView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(true);

		this.mWebView.setInitialScale(100);
		
		this.mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
		    	// Activities and WebViews measure progress with different scales.
		    	// The progress meter will automatically disappear when we reach 100%
				BrowserActivity.this.setProgress(progress * 100);
			}
		    
		    @Override
		    public void onReceivedTitle(WebView view, String title) {
		    	mTitle = title;
		    	setTitle(title);
		    }
		});
		
		// in case of a redirect don't open the external browser
		this.mWebView.setWebViewClient(new WebViewClient() {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        	view.loadUrl(url);
	            return true;
	        }

			@Override
			public void onPageFinished(WebView view, String url) {
				BrowserActivity.this.switchToPageView();
				super.onPageFinished(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				BrowserActivity.this.switchToLoadingView();
				super.onPageStarted(view, url, favicon);
			}
	        
		});
		
		this.mUri = getIntent().getData();
		this.mTitle = this.mUri.toString();
		this.setTitle(this.mTitle);

		this.mWebView.loadUrl(this.mUri.toString());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Must remove the WebView from the view system before destroying.
		this.mWebView.setVisibility(View.GONE);
		this.mWebView.destroy();
	}
	
	private void resetUI() {
		this.setTheme(this.mApplication.getSettings().getTheme());
		this.setContentView(R.layout.browser);
		
		this.mWebView = (WebViewFixed) findViewById(R.id.webview);
		this.mLoadingView = this.findViewById(R.id.loadingView);
		
		TypedArray a = this.mApplication.getTheme().obtainStyledAttributes(R.styleable.Theme);
		int background = a.getColor(R.styleable.Theme_activityRootBackground, 0);
		this.mWebView.setBackgroundColor(background);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && this.mWebView.canGoBack()) {
	    	this.mWebView.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
	        case R.id.open_browser_menu_id:
	    		if (this.mUri != null){
	    			BrowserLauncher.launchExternalBrowser(this, this.mUri.toString());
	    		}
	    		break;
	        case R.id.save_menu_id:
	        	this.mDownloadFileService.downloadFile(this, this.mUri.toString());
	        	break;
	    	case R.id.share_menu_id:
	    		Intent i = new Intent(Intent.ACTION_SEND);
	    		i.setType("text/plain");
	    		i.putExtra(Intent.EXTRA_TEXT, this.mUri);
	    		this.startActivity(Intent.createChooser(i, this.getString(R.string.share_via)));
	    		break;
    	}
    	
        return true;
    }

	@Override
	public File getCacheDir() {
		return this.getApplicationContext().getCacheDir();
	}
    
	private void switchToLoadingView(){
		this.switchToView(ViewType.LOADING);
	}
	
	private void switchToPageView(){
		this.switchToView(ViewType.PAGE);
	}
	
	private void switchToView(ViewType vt){
		if(vt == null) return;
		
		switch(vt){
			case PAGE:
				this.mWebView.setVisibility(View.VISIBLE);
				this.mLoadingView.setVisibility(View.GONE);
				break;
			case LOADING:
				this.mWebView.setVisibility(View.GONE);
				this.mLoadingView.setVisibility(View.VISIBLE);
				break;
		}
	}
    
}
