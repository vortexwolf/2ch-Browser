package com.vortexwolf.chan.services.presentation;

import android.view.View;
import android.view.View.OnLongClickListener;

import com.vortexwolf.chan.common.controls.ClickableURLSpan;
import com.vortexwolf.chan.interfaces.IURLSpanClickListener;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.services.BrowserLauncher;

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
