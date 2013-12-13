package com.vortexwolf.dvach.common.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.vortexwolf.dvach.common.library.MyLog;

/**
 * Fixes the onWindowFocusChanged bug, by catching NullPointerException.
 * https://groups.google.com/d/topic/android-developers/ktbwY2gtLKQ/discussion
 * 
 * @author Andrew
 * 
 */
public class WebViewFixed extends WebView {

    private static final String TAG = "WebView";

    public WebViewFixed(Context context) {
        super(context);
    }

    public WebViewFixed(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WebViewFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        try {
            super.onWindowFocusChanged(hasWindowFocus);
        } catch (NullPointerException ex) {
            MyLog.e(TAG, ex);
        }
    }
}
