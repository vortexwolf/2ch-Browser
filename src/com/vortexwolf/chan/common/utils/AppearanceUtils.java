package com.vortexwolf.chan.common.utils;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
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
    private static Point resolution = new Point();

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

    @SuppressLint("NewApi")
    public static void prepareWebView(WebView webView, int backgroundColor) {
        webView.setBackgroundColor(backgroundColor);
        webView.setInitialScale(100);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
        //settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
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
    
    public static void setScaleWebView(WebView webView, File file, Activity context) {
        int scale = 100;
        try {
        	Point imageSize = getImageSize(file);
            Point resolution = getResolution(webView);
            if (resolution.equals(0, 0)) {
            	Point displayResolution = getDisplayResolution(context);
            	if (AppearanceUtils.resolution.x == displayResolution.x || displayResolution.equals(0, 0)) {
            		resolution = AppearanceUtils.resolution;
            	} else {
            		resolution = displayResolution;
            		AppearanceUtils.resolution = resolution;
            	}
            } else AppearanceUtils.resolution = resolution;
            
            if (resolution.equals(0, 0)) throw new Exception("Cannot get screen resolution");
            
            MyLog.e(TAG, "Resolution: "+resolution.x+"x"+resolution.y);
            double scaleX = (double)resolution.x / (double)imageSize.x;
            double scaleY = (double)resolution.y / (double)imageSize.y;
            scale = (int)Math.round(Math.min(scaleX, scaleY) * 100d);
            MyLog.e(TAG, "Scale: "+(Math.min(scaleX, scaleY) * 100d));
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            webView.setInitialScale(scale);
            webView.setPadding(0, 0, 0, 0);
        }
    }
    
    private static Point getImageSize(File file) {
    	BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return new Point(options.outWidth, options.outHeight);
    }
    
    private static Point getResolution(View view) {
    	View viewParent = (View) view.getParent();
        return new Point(viewParent.getWidth(), viewParent.getHeight());
    }
    
    @SuppressLint("NewApi")
    private static Point getDisplayResolution(Activity context) {
    	Point mResolution = new Point();
    	Display display = context.getWindowManager().getDefaultDisplay();
    	if (Constants.SDK_VERSION >= 11) display.getSize(mResolution);
    	else mResolution.set(display.getWidth(), display.getHeight());
    	return mResolution;
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
