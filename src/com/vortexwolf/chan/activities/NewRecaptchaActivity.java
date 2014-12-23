package com.vortexwolf.chan.activities;

import com.vortexwolf.chan.common.library.MyLog;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewRecaptchaActivity extends Activity {
    private final static String data = 
            "<script src=\"https://www.google.com/recaptcha/api.js\" async defer></script>"+
            "<form action=\"_intercept\" method=\"GET\">"+
            "  <div class=\"g-recaptcha\" data-sitekey=\"6LcM2P4SAAAAAD97nF449oigatS5hPCIgt8AQanz\"></div>"+
            "  <input type=\"submit\" value=\"Submit\">"+
            "</form>";
    private final static String hashfilter = "g-recaptcha-response=";
    
    public final static int OK = 1;
    public final static int FAIL = 2;
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Recaptcha 2.0");
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.contains("_intercept")) {
                    if (url.indexOf(hashfilter) != -1) {
                        String hash = url.substring(url.indexOf(hashfilter) + hashfilter.length());
                        MyLog.d("INTERCEPTOR", "hash: "+hash);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("hash", hash);
                        setResult(OK, resultIntent);
                    } else {
                        setResult(FAIL);
                    }
                    finish();
                }
                return super.shouldInterceptRequest(view, url);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        setContentView(webView);
        webView.loadDataWithBaseURL("https://127.0.0.1/", data, "text/html", "UTF-8", null);
    }
}
