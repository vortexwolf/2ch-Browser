package ua.in.quireg.chan.views.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import ua.in.quireg.chan.common.library.MyLog;

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
    
    public boolean canScrollHorizontallyOldAPI(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }
}
