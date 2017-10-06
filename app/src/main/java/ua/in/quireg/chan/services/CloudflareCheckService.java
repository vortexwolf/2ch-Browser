package ua.in.quireg.chan.services;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.app.Activity;
import android.net.Uri;
import android.net.http.SslError;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.ExtendedHttpClient;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.interfaces.ICloudflareCheckListener;
import ua.in.quireg.chan.settings.ApplicationSettings;

import java.util.Timer;
import java.util.TimerTask;

public class CloudflareCheckService {
    private final String TAG="CloudflareCheckService";
    
    private ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private ExtendedHttpClient mHttpClient = (ExtendedHttpClient) Factory.resolve(DefaultHttpClient.class);
    private Activity mActivity;
    private ICloudflareCheckListener mListener = null;
    private String mUrl;
    
    private WebView mWebView = null;
    private Timer mTimeoutTimer = null;
    private boolean mIsRunning = false;
    
    public CloudflareCheckService(String url, Activity activity, ICloudflareCheckListener listener) {
        this.mUrl = url;
        this.mActivity = activity;
        this.mListener = listener;
    }

    public synchronized void start() {
        if (this.mIsRunning) {
            this.stop();
        }

        this.mIsRunning = true;

        mWebView = new WebView(mActivity);
        mWebView.setWillNotDraw(true);

        CookieSyncManager.getInstance().sync();
        CookieManager.getInstance().removeAllCookie();

        mWebView.setWebViewClient(new CloudflareWebViewClient());
        mWebView.getSettings().setUserAgentString(Constants.USER_AGENT_STRING);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);

        this.mTimeoutTimer = new Timer();
        this.mTimeoutTimer.schedule(new TimerTask() {
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onTimeout();
                    }
                });
            }
        }, 20000);

        mListener.onStart();
    }
    
    public synchronized void stop() {
        this.mIsRunning = false;

        if (this.mTimeoutTimer != null) {
            this.mTimeoutTimer.cancel();
            this.mTimeoutTimer = null;
        }

        if (this.mWebView != null) {
            this.mWebView.stopLoading();
            this.mWebView.clearCache(true);
            this.mWebView.destroy();
            this.mWebView = null;
        }
    }

    private synchronized void onTimeout() {
        if (!this.mIsRunning) {
            return;
        }

        this.mListener.onTimeout();
        this.stop();
    }

    private synchronized void onSuccess(BasicClientCookie cookie) {
        if (!this.mIsRunning) {
            return;
        }

        this.mHttpClient.removeCookie(Constants.CF_CLEARANCE_COOKIE);
        this.mHttpClient.setCookie(cookie);

        this.mSettings.saveCloudflareClearanceCookie(cookie);
        this.mListener.onSuccess();
        this.stop();
    }

    private class CloudflareWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);

            String cookieString = CookieManager.getInstance().getCookie(url);

            String value = null;
            String[] cookies = StringUtils.emptyIfNull(cookieString).split(";");
            for (String cookie : cookies) {
                String[] cookieNameValue = cookie.trim().split("=");
                if (StringUtils.areEqual(cookieNameValue[0], Constants.CF_CLEARANCE_COOKIE)) {
                    value = cookieNameValue[1];
                    break;
                }
            }

            if (value != null) {
                BasicClientCookie cfCookie = new BasicClientCookie(Constants.CF_CLEARANCE_COOKIE, value);
                cfCookie.setDomain("" + Uri.parse(url).getHost());
                cfCookie.setPath("/");

                CloudflareCheckService.this.onSuccess(cfCookie);
            } else {
                MyLog.d(TAG, "Cookie is not found");
            }
        }
    }
}