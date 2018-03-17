package ua.in.quireg.chan.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.models.presentation.GalleryItemViewBag;
import ua.in.quireg.chan.services.TimerService;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.views.TouchGifView;
import ua.in.quireg.chan.ui.views.WebViewFixed;

public class AppearanceUtils {

    public static void showLongToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static ListViewPosition getCurrentListPosition(ListView listView) {
        int index = 0;
        int top = 0;

        if (listView != null) {
            index = listView.getFirstVisiblePosition();
            View v = listView.getChildAt(0);
            top = (v == null) ? 0 : v.getTop();
        }

        return new ListViewPosition(index, top);
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

    public static int getThemeDependentColor(Theme theme, int styleableId) {
        TypedArray a = theme.obtainStyledAttributes(R.styleable.Theme);
        int color = a.getColor(styleableId, 0);
        a.recycle();

        return color;
    }

    public static void setImage(final File file, final Activity context, final FrameLayout layout, final int background) {
        setImage(file, context, layout, background, false);
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

        videoViewViewBag.videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);

            videoViewViewBag.durationView.setText(
                    String.format("00:00 / %s", formatVideoTime(mp.getDuration()))
            );

            viewBag.timer = new TimerService(1, context);
            viewBag.timer.runTask(() -> {
                try {
                    videoViewViewBag.durationView.setText(
                            String.format("%s / %s",
                            formatVideoTime(mp.getCurrentPosition()),
                            formatVideoTime(mp.getDuration()))
                    );
                } catch (Exception e) {
                    viewBag.timer.stop();
                }
            });

            if (settings.isVideoMute()) {
                mp.setVolume(0, 0);
                videoViewViewBag.setVolume(0);
            } else {
                mp.setVolume(1, 1);
                videoViewViewBag.setVolume(1);
            }
            videoViewViewBag.muteButton.setOnClickListener(v -> {
                if (videoViewViewBag.volume > 0) {
                    mp.setVolume(0, 0);
                    videoViewViewBag.setVolume(0);
                } else {
                    mp.setVolume(1, 1);
                    videoViewViewBag.setVolume(1);
                }
            });

            mp.start();
        });
        videoViewViewBag.videoView.setOnErrorListener((mp, what, extra) -> {
            Timber.e("Error code: %d", what);
            viewBag.switchToErrorView(context.getString(R.string.error_video_playing));
            return true;
        });

        videoViewViewBag.videoView.setVideoPath(file.getAbsolutePath());
    }

    public static void setWebViewFile(File file, Activity context, FrameLayout layout, int background) {
        WebViewFixed webView = new WebViewFixed(context);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.addView(webView);

        ApplicationSettings settings = Factory.getContainer().resolve(ApplicationSettings.class);
        Uri uri = Uri.fromFile(file);

        if (UriUtils.isImageUri(uri)) {
            AppearanceUtils.prepareWebViewForImage(webView, background);
            webView.loadDataWithBaseURL(null, createHtmlForImage(uri), "text/html", HTTP.UTF_8, null);
        } else if (UriUtils.isVideoUri(uri)) {
            String mutedAttr = settings.isVideoMute() ? "muted" : "";
            String attributes = String.format("src='%1$s' controls autoplay %2$s", uri, mutedAttr);

            AppearanceUtils.prepareWebViewForVideo(webView, background);
            webView.loadDataWithBaseURL(null, createHtmlForVideo(attributes), "text/html", HTTP.UTF_8, null);
        } else {
            webView.loadUrl(uri.toString());
        }
    }

    public static void callWhenLoaded(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Constants.SDK_VERSION < 16) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    CompatibilityUtils.removeOnGlobalLayoutListener(view, this);
                }

                runnable.run();
            }
        });
    }

    /**
     * Method creates StateListDrawable with original
     * and transparent bitmap to use with selector
     *
     * @param context  - app context
     * @param drawable - initial drawable
     * @return StateListDrawable with initial and transparent drawables
     */
    public static StateListDrawable getStateListDrawable(Context context, @DrawableRes int drawable) {

        Drawable stateSelected = ContextCompat.getDrawable(context, drawable);
        Drawable stateUnselected = stateSelected.getConstantState().newDrawable();

        stateUnselected.mutate();
        stateUnselected.setAlpha(126);

        StateListDrawable stateListDrawable = new StateListDrawable();

        stateListDrawable.addState(
                new int[]{android.R.attr.state_selected},
                stateSelected
        );
        stateListDrawable.addState(
                new int[]{android.R.attr.state_enabled},
                stateUnselected
        );

        return stateListDrawable;
    }

    private static void setImage(final File file, final Activity context, final FrameLayout layout, final int background, boolean forceWebView) {
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
                    gifView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    gifView.setBackgroundColor(background);
                    layout.addView(gifView);
                    isDone = true;
                }
            } else if (picMethod == Constants.IMAGE_VIEW_SUBSCALEVIEW
                    && !IoUtils.isNonStandardGrayscaleImage(file)) {
                final SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(context);

                imageView.setImage(ImageSource.uri(Uri.fromFile(file)));
                imageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
                    @Override
                    public void onTileLoadError(Exception e) {
                        AppearanceUtils.setImage(file, context, layout, background, true);
                    }

                    @Override
                    public void onImageLoadError(Exception e) {
                        AppearanceUtils.setImage(file, context, layout, background, true);
                    }
                });

                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setBackgroundColor(background);
                imageView.setMaxScale(4F);
                layout.addView(imageView);
                isDone = true;
            }
        } catch (Exception e) {
            Timber.e(e);
        } catch (OutOfMemoryError e) {
            Timber.e(e);
            System.gc();
        }

        if (!isDone) {
            setWebViewFile(file, context, layout, background);
        }
    }

    private static String createHtmlForVideo(String attributes) {
        String elementHtml = "<video" +
                " style='position:absolute;left:0;right:0;top:0;bottom:0;margin:auto;width:100%;height:100%;' " + attributes + ">" +
                "HTML5 video is not supported." +
                "</video>";

        return "<body style='margin:0;'>" + elementHtml + "</body>";
    }

    private static String createHtmlForImage(Uri uri) {
        StringBuffer img = new StringBuffer("<img src='" + uri + "'" +
                " style='position:absolute;left:0;right:0;top:0;bottom:0;margin:auto;width:0;height:0;' />");

        img.append("<script type='text/javascript'>");
        img.append("function updateImageSize() {");
        img.append("var img = document.getElementsByTagName('img')[0];");
        img.append("if(!img) return;");
        img.append("var widthRatio = img.clientWidth / window.innerWidth;");
        img.append("var heightRatio = img.clientHeight / window.innerHeight;");
        img.append("var isWide = widthRatio >= heightRatio;");
        img.append("img.style.height = isWide ? 'auto' : '100%';");
        img.append("img.style.width = isWide ? '100%' : 'auto';");
        img.append("}");

        // call when loaded and on each resize
        img.append("updateImageSize();");
        img.append("window.onload = updateImageSize;");
        img.append("window.addEventListener('resize', updateImageSize, false);");
        img.append("</script>");

        return "<body style='margin:0;'>" + img + "</body>";
    }

    private static String formatVideoTime(int milliseconds) {
        int seconds = milliseconds / 1000 % 60;
        int minutes = milliseconds / 60000;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private static void prepareWebViewForVideo(WebView webView, int backgroundColor) {
        webView.setBackgroundColor(backgroundColor);

        WebSettings settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Constants.SDK_VERSION >= 8) {
            CompatibilityUtils.setBlockNetworkLoads(settings, true);
        }
    }

    private static Drawable getThemeDrawable(Theme theme, int styleableId) {
        TypedArray a = theme.obtainStyledAttributes(R.styleable.Theme);
        Drawable drawable = a.getDrawable(styleableId);
        a.recycle();

        return drawable;
    }

    private static void prepareWebViewForImage(WebView webView, int backgroundColor) {
        webView.setBackgroundColor(backgroundColor);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);

        if (Constants.SDK_VERSION >= 5) {
            CompatibilityUtils.setScrollbarFadingEnabled(webView, true);
        }
        if (Constants.SDK_VERSION >= 8) {
            CompatibilityUtils.setBlockNetworkLoads(settings, true);
        }
        boolean isDisplayZoomControls = Factory.getContainer().resolve(ApplicationSettings.class).isDisplayZoomControls();
        CompatibilityUtils.setDisplayZoomControls(settings, isDisplayZoomControls);
    }

    public static class ListViewPosition implements Serializable {

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
            vb.videoView = (VideoView) container.findViewById(R.id.video_view);
            vb.durationView = (TextView) container.findViewById(R.id.video_duration);
            vb.muteButton = (ImageButton) container.findViewById(R.id.mute_speaker_image);

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
