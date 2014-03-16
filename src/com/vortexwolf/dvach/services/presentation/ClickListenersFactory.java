package com.vortexwolf.dvach.services.presentation;

import android.content.Context;
import android.net.Uri;
import android.os.Debug;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.models.presentation.AttachmentInfo;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class ClickListenersFactory {
    public static final OnLongClickListener sIgnoreOnLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    };

    public static IURLSpanClickListener getDefaultSpanClickListener(final DvachUriBuilder dvachUriBuilder) {
        return new IURLSpanClickListener() {
            @Override
            public void onClick(View v, String url) {
                Uri absoluteUri = dvachUriBuilder.adjust2chRelativeUri(Uri.parse(url));
                BrowserLauncher.launchExternalBrowser(v.getContext(), absoluteUri.toString());
            }
        };
    }
}
