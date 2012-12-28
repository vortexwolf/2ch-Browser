package com.vortexwolf.dvach.services;

import com.vortexwolf.dvach.activities.BrowserActivity;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Browser;

public class BrowserLauncher {

    private static void launchBrowser(Context context, String url, boolean useExternalBrowser) {

        Uri uri = Uri.parse(url);

        // Some URLs should always be opened externally, if BrowserActivity
        // doesn't support their content.
        if (!useExternalBrowser && !UriUtils.isImageUri(uri)) {
            useExternalBrowser = true;
        }

        if (useExternalBrowser) {
            Intent browser = new Intent(Intent.ACTION_VIEW, uri);
            browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            browser.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
            try {
                context.startActivity(browser);
            } catch (Exception e) {
                AppearanceUtils.showToastMessage(context, e.getMessage());
            }
        } else {
            Intent browser = new Intent(context, BrowserActivity.class);
            browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            browser.setData(uri);
            context.startActivity(browser);
        }
    }

    public static void launchExternalBrowser(Context context, String url) {
        launchBrowser(context, url, true);
    }

    public static void launchExternalBrowser(Context context, String url, boolean bypassManifestFilter) {
        if (bypassManifestFilter) {
            url = url.replaceFirst("2ch\\.so/", "2ch.so//");
            url = url.replaceFirst("2-ch\\.so/", "2-ch.so//");
        }

        launchExternalBrowser(context, url);
    }

    public static void launchInternalBrowser(Context context, String url) {
        launchBrowser(context, url, false);
    }
}
