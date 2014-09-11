package com.vortexwolf.chan.common.utils;

import java.io.File;

import pl.droidsonroids.gif.GifDrawable;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.FixedSubsamplingScaleImageView;
import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.controls.TouchGifView;
import com.vortexwolf.chan.common.controls.WebViewFixed;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.MainApplication;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class AppearanceUtils {

    private static final String TAG = "AppearanceUtils";
    
    public final static ViewGroup.LayoutParams MATCH_PARAMS = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

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
        if (Constants.SDK_VERSION >= 5) {
            CompatibilityUtilsImpl.setScrollbarFadingEnabled(webView, true);
        }

        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
        if (Constants.SDK_VERSION >= 7) {
            CompatibilityUtilsImpl.setDefaultZoomFAR(settings);
            CompatibilityUtilsImpl.setLoadWithOverviewMode(settings, true);
        }
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Constants.SDK_VERSION >= 8) {
            CompatibilityUtilsImpl.setBlockNetworkLoads(settings, true);
        }

        if (MainApplication.MULTITOUCH_SUPPORT && Constants.SDK_VERSION >= 11) {
            boolean isDisplayZoomControls = Factory.getContainer().resolve(ApplicationSettings.class).isDisplayZoomControls();
            CompatibilityUtilsImpl.setDisplayZoomControls(settings, isDisplayZoomControls);
        }
    }
    
    public static void setScaleWebView(final WebView webView, final View layout, final File file, final Activity context) {
        Runnable callSetScaleWebView = new Runnable() {
            @Override
            public void run() {
                setPrivateScaleWebView(webView, layout, file, context);
            }
        };
        
        Point resolution = getResolution(layout);
        if (resolution.equals(0, 0)) {
            // wait until the view is measured and its size is known
            callWhenLoaded(layout, callSetScaleWebView);
        } else {
            callSetScaleWebView.run();
        }
    }
    
    private static void setPrivateScaleWebView(WebView webView, View layout, File file, Activity context) {
        Point imageSize = getImageSize(file);
        Point resolution = getResolution(layout);
        
        //MyLog.d(TAG, "Resolution: "+resolution.x+"x"+resolution.y);
        double scaleX = (double)resolution.x / (double)imageSize.x;
        double scaleY = (double)resolution.y / (double)imageSize.y;
        int scale = (int)Math.round(Math.min(scaleX, scaleY) * 100d);
        scale = Math.max(scale, 1);
        //MyLog.d(TAG, "Scale: "+(Math.min(scaleX, scaleY) * 100d));
        if (Constants.SDK_VERSION >= 7) {
            double picdpi = (context.getResources().getDisplayMetrics().density * 160d) / scaleX;
            if (picdpi >= 240) {
                CompatibilityUtilsImpl.setDefaultZoomFAR(webView.getSettings());
            } else if (picdpi <= 120) {
                CompatibilityUtilsImpl.setDefaultZoomCLOSE(webView.getSettings());
            } else {
                CompatibilityUtilsImpl.setDefaultZoomMEDIUM(webView.getSettings());
            }
        }

        webView.setInitialScale(scale);
        webView.setPadding(0, 0, 0, 0);
    }
    
    private static Point getImageSize(File file) {
        return IoUtils.getImageSize(file);
    }
    
    private static Point getResolution(View view) {
        return new Point(view.getWidth(), view.getHeight());
    }
    
    public static int getThemeColor(Theme theme, int styleableId) {
        TypedArray a = theme.obtainStyledAttributes(R.styleable.Theme);
        int color = a.getColor(styleableId, 0);
        a.recycle();

        return color;
    }
    
    public static void setImage(final File file, final Activity context, final FrameLayout layout, final int background) {
        setImage(file, context, layout, background, false);
    }
    
    public static void setImage(final File file, final Activity context, final FrameLayout layout, final int background, boolean forceWebView) {
        final ApplicationSettings mSettings = Factory.getContainer().resolve(ApplicationSettings.class);
        int gifMethod = forceWebView ? Constants.GIF_VIEW_DEFAULT : mSettings.getGifView();
        int picMethod = forceWebView ? Constants.IMAGE_VIEW_DEFAULT : mSettings.getImageView();
        
        boolean isDone = false;
        
        layout.removeAllViews();
        System.gc();
        
        try {
            if (RegexUtils.getFileExtension(file.getAbsolutePath()).equalsIgnoreCase("gif")) {
                if (gifMethod == Constants.GIF_NATIVE_LIB) {
                    GifDrawable gifDrawable = new GifDrawable(file.getAbsolutePath());
                    ImageView gifView;
                    if (Constants.SDK_VERSION >= 8) {
                        gifView = new TouchGifView(context);
                    } else {
                        gifView = new ImageView(context);
                    }
                    gifView.setImageDrawable(gifDrawable);
                    gifView.setLayoutParams(MATCH_PARAMS);
                    gifView.setBackgroundColor(background);
                    layout.addView(gifView);
                    isDone = true;
                }
            } else if (picMethod == Constants.IMAGE_VIEW_SUBSCALEVIEW && Constants.SDK_VERSION >= 10) {
                final FixedSubsamplingScaleImageView imageView = new FixedSubsamplingScaleImageView(context);
                imageView.setImageFile(file.getAbsolutePath(), new FixedSubsamplingScaleImageView.InitedCallback() {
                    @Override
                    public void onInit() {
                        AppearanceUtils.setImage(file, context, layout, background, true);
                    }
                });
                imageView.setLayoutParams(MATCH_PARAMS);
                imageView.setBackgroundColor(background);
                layout.addView(imageView);
                imageView.setInitCallback(new FixedSubsamplingScaleImageView.InitedCallback() {   
                    @Override
                    public void onInit() {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run(){
                                layout.removeAllViews();
                                layout.addView(imageView);
                            }
                        });
                    }
                });
                isDone = true;
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } catch (OutOfMemoryError e) {
            MyLog.e(TAG, e);
            System.gc();
        }
        if (!isDone) {
            WebViewFixed wV = new WebViewFixed(context);
            wV.setLayoutParams(MATCH_PARAMS);
            layout.addView(wV);
            AppearanceUtils.prepareWebView(wV, background);
            AppearanceUtils.setScaleWebView(wV, layout, file, context);
            wV.loadUrl(Uri.fromFile(file).toString());
        }
    }
    
    public static void callWhenLoaded(final View view, final Runnable runnable){
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(Constants.SDK_VERSION < 16) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    CompatibilityUtilsImpl.removeOnGlobalLayoutListener(view, this);
                }
                
                runnable.run();
            }
        });
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
