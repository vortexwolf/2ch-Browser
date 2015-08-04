package com.vortexwolf.chan.common.utils;

import java.io.File;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.davemorrissey.labs.subscaleview.FixedSubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.MainApplication;
import com.vortexwolf.chan.common.controls.TouchGifView;
import com.vortexwolf.chan.common.controls.WebViewFixed;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.models.presentation.GalleryItemViewBag;
import com.vortexwolf.chan.services.TimerService;
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

    public static Drawable getThemeDrawable(Theme theme, int styleableId) {
        TypedArray a = theme.obtainStyledAttributes(R.styleable.Theme);
        Drawable drawable = a.getDrawable(styleableId);
        a.recycle();

        return drawable;
    }

    public static void setImage(final File file, final Activity context, final FrameLayout layout, final int background) {
        setImage(file, context, layout, background, false);
    }

    public static void setImage(final File file, final Activity context, final FrameLayout layout, final int background, boolean forceWebView) {
        final ApplicationSettings mSettings = Factory.getContainer().resolve(ApplicationSettings.class);
        int gifMethod = forceWebView ? Constants.GIF_WEB_VIEW : mSettings.getGifView();
        int picMethod = forceWebView ? Constants.IMAGE_VIEW_WEB_VIEW : mSettings.getImageView();

        boolean isDone = false;

        layout.removeAllViews();

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
            } else if (picMethod == Constants.IMAGE_VIEW_SUBSCALEVIEW
                    && !IoUtils.isNonStandardGrayscaleImage(file)) {
                final FixedSubsamplingScaleImageView imageView = new FixedSubsamplingScaleImageView(context);

                imageView.setImage(ImageSource.uri(Uri.fromFile(file)));
                imageView.setOnImageEventListener(new FixedSubsamplingScaleImageView.DefaultOnImageEventListener() {
                    @Override
                    public void onTileLoadError(Throwable e) {
                        AppearanceUtils.setImage(file, context, layout, background, true);
                    }
                    @Override
                    public void onImageLoadError(Throwable e) {
                        AppearanceUtils.setImage(file, context, layout, background, true);
                    }
                });

                imageView.setLayoutParams(MATCH_PARAMS);
                imageView.setBackgroundColor(background);
                imageView.setMaxScale(4F);
                layout.addView(imageView);
                isDone = true;
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } catch (OutOfMemoryError e) {
            MyLog.e(TAG, e);
            System.gc();
        }

        if (!isDone) {
            setWebViewFile(file, context, layout, background);
        }
    }

    public static void setVideoFile(final File file, final Activity context, final GalleryItemViewBag viewBag, final int background, final Theme theme) {
        final ApplicationSettings settings = Factory.getContainer().resolve(ApplicationSettings.class);
        viewBag.layout.removeAllViews();

        if (settings.getVideoPlayer() == Constants.VIDEO_PLAYER_WEBVIEW) {
            setWebViewFile(file, context, viewBag.layout, background);
            return;
        }

        View container = LayoutInflater.from(context).inflate(R.layout.video_view, viewBag.layout);

        final VideoViewViewBag videoViewViewBag = VideoViewViewBag.fromContainer(container);
        videoViewViewBag.speakerDrawable = getThemeDrawable(theme, R.styleable.Theme_iconSoundSpeaker);
        videoViewViewBag.muteDrawable = getThemeDrawable(theme, R.styleable.Theme_iconSoundMute);

        videoViewViewBag.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                mp.setLooping(true);

                videoViewViewBag.durationView.setText("00:00 / " + formatVideoTime(mp.getDuration()));

                viewBag.timer = new TimerService(1, context);
                viewBag.timer.runTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            videoViewViewBag.durationView.setText(formatVideoTime(mp.getCurrentPosition()) +
                                " / " + formatVideoTime(mp.getDuration()));
                        } catch (Exception e) {
                            viewBag.timer.stop();
                        }
                    }
                });

                if (settings.isVideoMute()) {
                    mp.setVolume(0, 0);
                    videoViewViewBag.setVolume(0);
                } else {
                    mp.setVolume(1, 1);
                    videoViewViewBag.setVolume(1);
                }
                videoViewViewBag.muteButton.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (videoViewViewBag.volume > 0) {
                            mp.setVolume(0, 0);
                            videoViewViewBag.setVolume(0);
                        } else {
                            mp.setVolume(1, 1);
                            videoViewViewBag.setVolume(1);
                        }
                    }
                });

                mp.start();
            }
        });
        videoViewViewBag.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                MyLog.e(TAG, "Error code: " + what);
                viewBag.switchToErrorView(context.getString(R.string.error_video_playing));
                return true;
            }
        });

        videoViewViewBag.videoView.setVideoPath(file.getAbsolutePath());
    }

    public static void setWebViewFile(File file, Activity context, FrameLayout layout, int background) {
        WebViewFixed wV = new WebViewFixed(context);
        wV.setLayoutParams(MATCH_PARAMS);
        layout.addView(wV);

        Uri uri = Uri.fromFile(file);

        if (UriUtils.isImageUri(uri)) {
            AppearanceUtils.prepareWebView(wV, background);
            AppearanceUtils.setScaleWebView(wV, layout, file, context);
            wV.loadUrl(Uri.fromFile(file).toString());
        } else if (UriUtils.isWebmUri(uri)) {
            ApplicationSettings settings = Factory.getContainer().resolve(ApplicationSettings.class);

            String mutedAttr = settings.isVideoMute() ? "muted" : "";
            String attributes = String.format("src='%1$s' controls autoplay %2$s", uri, mutedAttr);

            wV.setBackgroundColor(background);
            wV.loadDataWithBaseURL(null, createHtmlForElement("video", attributes, "HTML5 video is not supported.", true),
                "text/html; charset=UTF-8", null, null);
        } else {
            wV.loadUrl(uri.toString());
        }
    }

    private static String createHtmlForElement(String elementName, String attributes, String content, boolean isContentElement) {
        StringBuffer elementHtml = new StringBuffer("<" + elementName +
            " style='position:absolute;left:0;right:0;top:0;bottom:0;margin:auto;width:100%;height:100%;' " + attributes);
        if (isContentElement) {
            elementHtml.append(">" + content + "</" + elementName + ">");
        } else {
            elementHtml.append("/>");
        }

        return "<body style='margin:0;'>" + elementHtml.toString() + "</body>";
    }

    private static String formatVideoTime(int milliseconds) {
        int seconds = milliseconds / 1000 % 60;
        int minutes = milliseconds / 60000;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
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

    private static class VideoViewViewBag {
        public View container;
        public VideoView videoView;
        public TextView durationView;
        public ImageButton muteButton;
        public Drawable speakerDrawable;
        public Drawable muteDrawable;
        public float volume;

        public static VideoViewViewBag fromContainer(View container) {
            VideoViewViewBag vb = new VideoViewViewBag();
            vb.container = container;
            vb.videoView = (VideoView)container.findViewById(R.id.video_view);
            vb.durationView = (TextView)container.findViewById(R.id.video_duration);
            vb.muteButton = (ImageButton)container.findViewById(R.id.mute_speaker_image);

            return vb;
        }

        public void setVolume(float volume) {
            this.volume = volume;
            if (this.volume > 0) {
                this.muteButton.setImageDrawable(this.speakerDrawable);
            } else {
                this.muteButton.setImageDrawable(this.muteDrawable);
            }
        }
    }
}
