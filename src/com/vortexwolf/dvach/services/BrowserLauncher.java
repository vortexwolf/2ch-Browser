package com.vortexwolf.dvach.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

import com.vortexwolf.dvach.activities.BrowserActivity;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;

public class BrowserLauncher {
    public static void launchExternalBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        
        Intent browser = new Intent(Intent.ACTION_VIEW, uri);
        browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        browser.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        try {
            context.startActivity(browser);
        } catch (Exception e) {
            AppearanceUtils.showToastMessage(context, e.getMessage());
        }
    }

    public static void launchInternalBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        
        if(!UriUtils.isImageUri(uri)) {
            launchExternalBrowser(context, url);
            return;
        }
        
        Intent browser = new Intent(context, BrowserActivity.class);
        browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        browser.setData(uri);
        context.startActivity(browser);
    }
}
