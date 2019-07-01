package ua.in.quireg.chan.common.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.text.Spannable;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.PopupMenu;

import java.util.concurrent.Executor;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.models.presentation.PostItemViewModel;
import ua.in.quireg.chan.ui.views.MyLeadingMarginSpan2;

import static ua.in.quireg.chan.ui.fragments.PostsListFragment.handleContextMenuItemClick;
import static ua.in.quireg.chan.ui.fragments.PostsListFragment.populateContextMenu;

public class CompatibilityUtils {

    public static void copyText(Context context, String label, String text) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
        }
    }

    public static boolean isTablet(Context context) {
        return CompatibilityUtils.API4.isTablet(context);
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

    public static void setBlockNetworkLoads(WebSettings settings, boolean flag) {
        settings.setBlockNetworkLoads(flag);
    }

    public static void setDisplayZoomControls(WebSettings settings, boolean enabled) {
        settings.setDisplayZoomControls(enabled);
    }

    public static void removeOnGlobalLayoutListener(View view,
                                                    ViewTreeObserver.OnGlobalLayoutListener v) {
        view.getViewTreeObserver().removeOnGlobalLayoutListener(v);
    }

    public static void setDimAmount(Window window, float f) {
        window.setDimAmount(f);
    }

    public static void setSerialExecutor() {
        try {
            AsyncTask.class.getMethod("setDefaultExecutor", Executor.class)
                    .invoke(null, AsyncTask.SERIAL_EXECUTOR);
        } catch (Exception e) {
            MyLog.e("setDefaultExecutor", e);
        }
    }

    public static class API4 {

        public static boolean isTablet(Context context) {
            return (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        }
    }

    public static View.OnClickListener createClickListenerShowPostMenu(final Activity activity,
                                                                       final PostItemViewModel model,
                                                                       final View view) {
        return v -> {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            Menu menu = popupMenu.getMenu();
            populateContextMenu(menu, model, Factory.resolve(Resources.class));
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                handleContextMenuItemClick(menuItem, model, activity, view);
                return true;
            });
            popupMenu.show();
        };
    }

    public static void resetMyLeadingMarginSpanState(Spannable ss) {
        MyLeadingMarginSpan2[] spans = ss.getSpans(0, ss.length(), MyLeadingMarginSpan2.class);
        for (MyLeadingMarginSpan2 span : spans) {
            span.resetDrawState();
        }
    }

    public static void setMyLeadingMarginSpanCurrentLine(Spannable ss, int line) {
        MyLeadingMarginSpan2[] spans = ss.getSpans(0, ss.length(), MyLeadingMarginSpan2.class);
        for (MyLeadingMarginSpan2 span : spans) {
            span.setMyLeadingMarginSpanCurrentLine(line);
        }
    }

    public static void callMyLeadingMarginSpanMeasure(Spannable ss) {
        MyLeadingMarginSpan2[] spans = ss.getSpans(0, ss.length(), MyLeadingMarginSpan2.class);
        for (MyLeadingMarginSpan2 span : spans) {
            span.onMeasure();
        }
    }
}
