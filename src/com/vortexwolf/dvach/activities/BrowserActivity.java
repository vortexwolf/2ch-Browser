package com.vortexwolf.dvach.activities;
import java.io.File;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.controls.WebViewFixed;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.Tracker;

import android.app.Activity;
import android.content.res.TypedArray;
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
	
    private MainApplication mApplication;
	private IDownloadFileService mDownloadFileService;
	private Tracker mTracker;
	
	private WebView mWebview;
	private Uri mUri = null;
	private String mTitle = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        this.mApplication = (MainApplication) this.getApplication();
        this.mDownloadFileService = this.mApplication.getDownloadFileService();
        this.mTracker = this.mApplication.getTracker();
        
        this.mTracker.trackActivityView(TAG);
        
        this.resetUI();
        
		WebSettings settings = mWebview.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(true);

		this.mWebview.setInitialScale(100);
		
		this.mWebview.setWebChromeClient(new WebChromeClient() {
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
		this.mWebview.setWebViewClient(new WebViewClient() {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        	view.loadUrl(url);
	            return true;
	        }
		});
		
		this.mUri = getIntent().getData();
		this.mTitle = this.mUri.toString();
		this.setTitle(this.mTitle);
		
		if (savedInstanceState != null) {
			this.mWebview.restoreState(savedInstanceState);
		} else {
			this.mWebview.loadUrl(this.mUri.toString());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Must remove the WebView from the view system before destroying.
		this.mWebview.setVisibility(View.GONE);
		this.mWebview.destroy();
	}
	
	private void resetUI() {
		this.setTheme(this.mApplication.getSettings().getTheme());
		this.setContentView(R.layout.browser);
		
		this.mWebview = (WebViewFixed) findViewById(R.id.webview);
		
		TypedArray a = this.mApplication.getTheme().obtainStyledAttributes(R.styleable.Theme);
		int background = a.getColor(R.styleable.Theme_activityRootBackground, 0);
		this.mWebview.setBackgroundColor(background);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && this.mWebview.canGoBack()) {
	    	this.mWebview.goBack();
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
	        case R.id.save_menu_id: {
	            // Пробую сгенерировать ссылку на сохраненный файл так же, как это делает WebView
	            String hashCode = String.format("%08x", this.mUri.hashCode());
	            File file = new File(new File(getCacheDir(), "webviewCache"), hashCode);
	            
	        	this.mDownloadFileService.downloadFile(this, this.mUri.toString(), file.exists() ? file : null);
	        	break;
	        }
    	}
    	
        return true;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	this.mWebview.saveState(outState);
    }

	@Override
	public File getCacheDir() {
		return this.getApplicationContext().getCacheDir();
	}
    
    
}
