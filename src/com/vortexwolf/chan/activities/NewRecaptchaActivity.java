package com.vortexwolf.chan.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vortexwolf.chan.common.library.MyLog;

public class NewRecaptchaActivity extends Activity {

    private final static String data = 
            "<script src=\"https://www.google.com/recaptcha/api.js\" async defer></script>"+
            "<form action=\"_intercept\" method=\"GET\">"+
                    "  <div class=\"g-recaptcha\" data-sitekey=\"6LeQYz4UAAAAAL8JCk35wHSv6cuEV5PyLhI6IxsM\"></div>" +
            "  <input type=\"submit\" value=\"Submit\">"+
            "</form>";

    private final static String hashfilter = "g-recaptcha-response=";

    private static final String DOMAIN = "https://2ch.hk/";
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Recaptcha 2.0");
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.contains("_intercept")) {
                    if (url.contains(hashfilter)) {
                        String hash = url.substring(url.indexOf(hashfilter) + hashfilter.length());
                        MyLog.d("INTERCEPTOR", "hash: "+hash);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("hash", hash);
                        setResult(RESULT_OK, resultIntent);
                    } else {
                        setResult(RESULT_CANCELED);
                    }
                    finish();
                }
                return super.shouldInterceptRequest(view, url);
            }
        });
        webView.loadDataWithBaseURL(DOMAIN, data, "text/html", "UTF-8", null);
        setContentView(webView);

    }
}
