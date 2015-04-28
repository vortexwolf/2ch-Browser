package com.vortexwolf.chan.common.utils;

import java.util.concurrent.Executor;

import com.vortexwolf.chan.activities.PostsListActivity;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.models.presentation.PostItemViewModel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.PopupMenu;
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
        view.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
    }

    public static void setDimAmount(Window window, float f) {
        window.setDimAmount(f);
    }

    public static void setSerialExecutor() {
        try {
            AsyncTask.class.getMethod("setDefaultExecutor", Executor.class).invoke(null, AsyncTask.SERIAL_EXECUTOR);
        } catch (Exception e) {
            MyLog.e("setDefaultExecutor", e);
        }        
    }

    public static class API4 {
        public static boolean isTablet(Context context) {
            return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        }
    }

    public static View.OnClickListener createClickListenerShowPostMenu(final Activity activity, final PostItemViewModel model, final View view) {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(activity, v);
                Menu menu = popupMenu.getMenu();
                PostsListActivity.populateContextMenu(menu, model, Factory.resolve(Resources.class));
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        PostsListActivity.handleContextMenuItemClick(menuItem, model, activity, view);
                        return true;
                    }
                });
                popupMenu.show();
            }
        };
    }
}
