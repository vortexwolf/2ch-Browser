package ua.in.quireg.chan.services.presentation;

import android.view.View;
import android.view.View.OnLongClickListener;

import ua.in.quireg.chan.views.controls.ClickableURLSpan;
import ua.in.quireg.chan.interfaces.IURLSpanClickListener;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.services.BrowserLauncher;

public class ClickListenersFactory {
    public static final OnLongClickListener sIgnoreOnLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    };

    public static IURLSpanClickListener getDefaultSpanClickListener(final IUrlBuilder urlBuilder) {
        return new IURLSpanClickListener() {
            @Override
            public void onClick(View v, ClickableURLSpan span, String url) {
                BrowserLauncher.launchExternalBrowser(v.getContext(), urlBuilder.makeAbsolute(url));
            }
        };
    }
}
