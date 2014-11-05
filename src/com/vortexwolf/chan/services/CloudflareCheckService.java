package com.vortexwolf.chan.services;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.net.http.SslError;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.interfaces.ICloudflareCheckListener;
import com.vortexwolf.chan.interfaces.IListView;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class CloudflareCheckService {
    private final String TAG="CloudflareCheckService";
    
    private ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private ExtendedHttpClient mHttpClient = (ExtendedHttpClient) Factory.resolve(DefaultHttpClient.class);
    private Activity mActivity;
    private ViewGroup mLayout;
    private ICloudflareCheckListener mListener = null;
    private String url;
    
    private static boolean isActive = false;
    
    private WebView mWebView = null;
    
    private WebViewClient mClient = new WebViewClient() {
        @Override
        public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
        
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            MyLog.d(TAG, "Got Page: "+url);
            String value = null;
            try {
                String[] cookies = CookieManager.getInstance().getCookie(url).split("[;]");
                for (String cookie : cookies) {
                    if ((!StringUtils.isEmptyOrWhiteSpace(cookie)) && (cookie.startsWith(" "+Constants.CF_CLEARANCE_COOKIE+"="))) {
                        value = cookie.substring(Constants.CF_CLEARANCE_COOKIE.length() + 2);
                    }
                }
            } catch (NullPointerException e) {
                MyLog.e(TAG, e);
            }
            if (value != null) {
                BasicClientCookie cf_cookie = new BasicClientCookie(Constants.CF_CLEARANCE_COOKIE, value);
                cf_cookie.setDomain("."+Uri.parse(url).getHost());
                cf_cookie.setPath("/");
                mHttpClient.setCookie(cf_cookie);
                mSettings.saveCloudflareClearanceCookie(cf_cookie);
                mListener.onSuccess();
                MyLog.d(TAG, "Cookie found: "+value);
                stop();
            } else { MyLog.d(TAG, "Cookie is not found"); }
        }
    };
    
    public CloudflareCheckService(String url, Activity activity, ICloudflareCheckListener listener) {
        this.url = url;
        mActivity = activity;
        mLayout = (ViewGroup)activity.getWindow().getDecorView().getRootView();
        this.mListener = listener;
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    public void start() {
        if (!isActive) {
            isActive = true;
            MyLog.d(TAG, "Started CF checking");
            mWebView = new WebView(mActivity);
            mWebView.setVisibility(View.GONE);
            mLayout.addView(mWebView);
            CookieSyncManager.createInstance(mActivity);
            CookieManager.getInstance().removeAllCookie();
            mWebView.setWebViewClient(mClient);
            mWebView.getSettings().setUserAgentString(Constants.USER_AGENT_STRING);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.loadUrl(url);
            new CountDownTimer(20000, 1000) {
                public void onTick(long millisUntilFinished) { }
                public void onFinish() {
                    if (isActive) {
                        CloudflareCheckService.this.stop();
                        mListener.onTimeout();
                    }
                }
            }.start();
            mListener.onStart();
        }
    }
    
    public void stop() {
        if (mWebView != null && mLayout != null) mLayout.removeView(mWebView);
        if (mWebView != null) mWebView.stopLoading();
        if (mWebView != null) mWebView.clearCache(true);
        if (mWebView != null) mWebView.destroy();
        if (mWebView != null) mWebView = null;
        isActive = false;
        MyLog.d(TAG, "Task finished");
    }

}