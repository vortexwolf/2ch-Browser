package com.vortexwolf.chan.common.utils;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.MainApplication;

public class AppearanceUtils {

    private static final String TAG = "AppearanceUtils";

    public static void showToastMessage(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public static ListViewPosition getCurrentListPosition(ListView listView) {
        int index = 0;
        int top = 0;

        if (listView != null) {
            index = listView.getFirstVisiblePosition();
            View v = listView.getChildAt(0);
            top = (v == null) ? 0 : v.getTop();
        }

        ListViewPosition position = new ListViewPosition(index, top);
        return position;
    }

    public static View getListItemAtPosition(ListView listView, int position) {
        int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount(); // This is the same as child #0
        int wantedChild = position - firstPosition;

        if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
            return null;
        }

        // Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
        return listView.getChildAt(wantedChild);
    }

    public static void showImageProgressBar(final View indeterminateProgressBar, final ImageView imageView) {
        if (indeterminateProgressBar != null) {
            indeterminateProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public static void hideImageProgressBar(final View indeterminateProgressBar, final ImageView imageView) {
        if (indeterminateProgressBar != null) {
            indeterminateProgressBar.setVisibility(View.GONE);
        }
    }

    public static void clearImage(ImageView image) {
        image.setImageResource(android.R.color.transparent);
    }

    public static Bitmap reduceBitmapSize(Resources resources, Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int oldSize = Math.max(width, height);
        int newSize = resources.getDimensionPixelSize(R.dimen.thumbnail_size);

        float scale = newSize / (float) oldSize;

        if (scale >= 1.0) {
            return bitmap;
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
        bitmap.recycle();
        return resizedBitmap;
    }

    public static void prepareWebView(WebView webView, int backgroundColor) {
        webView.setBackgroundColor(backgroundColor);
        webView.setInitialScale(100);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
        settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        //settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Constants.SDK_VERSION >= 8) {
            settings.setBlockNetworkLoads(true);
        }

        if (MainApplication.MULTITOUCH_SUPPORT && Constants.SDK_VERSION >= 11) {
            //settings.setDisplayZoomControls(false);
        }
    }
    
    public static void setScaleWebView(WebView webView, File file) {
        int scale = 100;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            Point imageSize = new Point(options.outWidth, options.outHeight);
            
            View webViewParent = (View)webView.getParent();
            Point resolution = new Point(webViewParent.getWidth(), webViewParent.getHeight());
            
            double scaleX = (double)resolution.x / (double)imageSize.x;
            double scaleY = (double)resolution.y / (double)imageSize.y;
            scale = (int)(Math.min(scaleX, scaleY) * 100d);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            //webView.setInitialScale(Math.min(scale, 100));
            webView.setInitialScale(scale);
            webView.setPadding(0, 0, 0, 0);
        }
    }

    public static int getThemeColor(Theme theme, int styleableId) {
        TypedArray a = theme.obtainStyledAttributes(R.styleable.Theme);
        int color = a.getColor(styleableId, 0);
        a.recycle();

        return color;
    }

    public static class ListViewPosition {

        public ListViewPosition(int position, int top) {
            this.position = position;
            this.top = top;
        }

        public int position;
        public int top;
    }
}
