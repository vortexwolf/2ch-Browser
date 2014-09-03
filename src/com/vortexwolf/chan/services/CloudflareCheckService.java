package com.vortexwolf.chan.services;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.interfaces.ICloudflareListener;
import com.vortexwolf.chan.interfaces.IListView;
import com.vortexwolf.chan.interfaces.IPostSendView;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class CloudflareCheckService {
    private final String TAG="Cloudflare";
    
    private ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private ExtendedHttpClient mHttpClient = (ExtendedHttpClient) Factory.resolve(DefaultHttpClient.class);
    private Activity mActivity;
    private ViewGroup mLayout;
    private ICloudflareListener mListener = null;
    private IListView mListView = null;
    private String url;
    
    private static boolean isActive = false;
    
    private WebView mWebView = null;
    
    private WebViewClient mClient = new WebViewClient() {
    	public void onPageFinished(WebView webView, String url) {
   			super.onPageFinished(webView, url);
   			MyLog.d(TAG, "Got Page: "+url);
   			String value = null;
   			String[] cookies = CookieManager.getInstance().getCookie(url).split("[;]");
   			for (String cookie : cookies)
   				if ((!StringUtils.isEmptyOrWhiteSpace(cookie)) && (cookie.startsWith(" "+Constants.CF_CLEARANCE_COOKIE+"=")))
   					value = cookie.substring(Constants.CF_CLEARANCE_COOKIE.length() + 2);
   			
   			if (value != null) {
   				BasicClientCookie cf_cookie = new BasicClientCookie(Constants.CF_CLEARANCE_COOKIE, value);
   				cf_cookie.setDomain("."+Uri.parse(url).getHost());
   				cf_cookie.setPath("/");
   				mHttpClient.setCookie(cf_cookie);
   				mSettings.saveCloudflareClearanceCookie(cf_cookie);
   				showMessage(mActivity.getString(R.string.notification_cloudflare_finished));
   				if (mListener != null) mListener.success();
   				MyLog.d(TAG, "Cookie found: "+value);
   				stop();
   			} else { MyLog.d(TAG, "Cookie is not found"); }
    	}
    };
	
    private void showMessage(String message) {
    	if (mListView != null) { mListView.showError(message); }
    	else { AppearanceUtils.showToastMessage(mActivity, message);}
    }
	    
    public CloudflareCheckService(String url, Activity activity) {
    	this.url = url;
    	mActivity = activity;
    	mLayout = (ViewGroup)activity.getWindow().getDecorView().getRootView();
    }
    
    public CloudflareCheckService(String url, Activity activity, ICloudflareListener listener) {
    	this(url, activity);
    	this.mListener = listener;
    }
    
    public CloudflareCheckService(String url, Activity activity, ICloudflareListener listener, IListView listView) {
    	this(url, activity, listener);
    	this.mListView = listView;
    }
	
    @SuppressLint("SetJavaScriptEnabled")
    public void start() {
   		if (!isActive) {
   			isActive = true;
   			MyLog.d(TAG, "Started CF checking");
   			showMessage(mActivity.getString(R.string.notification_cloudflare_started));
   			mWebView = new WebView(mActivity);
   			mWebView.setVisibility(View.GONE);
   			mLayout.addView(mWebView);
   			CookieSyncManager.createInstance(mActivity);
   			CookieManager.getInstance().removeAllCookie();
   			mWebView.setWebViewClient(mClient);
   			mWebView.getSettings().setUserAgentString(Constants.USER_AGENT_STRING);
  			mWebView.getSettings().setJavaScriptEnabled(true);
   			mWebView.loadUrl(url);
   		}
    }
    
    public void stop() {
    	mLayout.removeView(mWebView);
    	mWebView.stopLoading();
    	mWebView.clearCache(true);
    	mWebView.destroy();
    	mWebView = null;
    	isActive = false;
    	MyLog.d(TAG, "Task finished");
    }

}