package ua.in.quireg.chan.services.presentation;

import android.view.View.OnLongClickListener;

import ua.in.quireg.chan.interfaces.IURLSpanClickListener;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.services.BrowserLauncher;

public class ClickListenersFactory {
    public static final OnLongClickListener sIgnoreOnLongClickListener = v -> false;

    public static IURLSpanClickListener getDefaultSpanClickListener(final IUrlBuilder urlBuilder) {
        return (v, span, url) -> BrowserLauncher.launchExternalBrowser(v.getContext(), urlBuilder.makeAbsolute(url));
    }
}
