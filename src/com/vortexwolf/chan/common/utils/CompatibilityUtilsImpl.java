package com.vortexwolf.chan.common.utils;

import com.vortexwolf.chan.common.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * I use 2 classes because I still support the version 1.5 which throws
 * VerifyException if to put all code in the single class
 */
@SuppressLint("NewApi")
public class CompatibilityUtilsImpl {

    public static boolean hasMultitouchSupport(PackageManager packageManager) {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
    }
    
    public static String getDocumentId(Uri uri) {
        return DocumentsContract.getDocumentId(uri);
    }
    
    public static boolean isDocumentUri(Context context, Uri uri) {
        return DocumentsContract.isDocumentUri(context, uri);
    }
    
    public static void displayGetSize(Display display, Point point) {
        display.getSize(point);
    }
    
    public static void setScrollbarFadingEnabled(WebView webView, boolean fadeScrollbars) {
        webView.setScrollbarFadingEnabled(fadeScrollbars);
    }
    
    public static void setDefaultZoomFAR(WebSettings settings) {
        settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
    }
    
    public static void setDefaultZoomCLOSE(WebSettings settings) {
        settings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
    }
    
    public static void setDefaultZoomMEDIUM(WebSettings settings) {
        settings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
    }
    
    public static void setLoadWithOverviewMode(WebSettings settings, boolean overview) {
        settings.setLoadWithOverviewMode(overview);
    }
    
    public static void setBlockNetworkLoads(WebSettings settings, boolean flag) {
        settings.setBlockNetworkLoads(flag);
    }
    
    public static void setDisplayZoomControls(WebSettings settings, boolean enabled) {
        settings.setDisplayZoomControls(enabled);
    }
    
    public static void setDisplayHomeAsUpEnabled(Activity activity) {
        activity.getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static boolean hasHardwareMenu(Context context, int currentVersion) {
        if (currentVersion < 11) {
            return true;
        } else if (currentVersion >= 11 && currentVersion <= 13) {
            return false;
        }

        return ViewConfiguration.get(context).hasPermanentMenuKey();
    }

    public static boolean isTextSelectable(TextView textView) {
        return textView.isTextSelectable();
    }

    public static void copyText(Activity activity, String label, String text, int currentVersion) {
        if (currentVersion < 11) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        }
    }
    
    public static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener victim) {
        if(Constants.SDK_VERSION < 16) {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(victim);
        } else {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
        }
    }
}
