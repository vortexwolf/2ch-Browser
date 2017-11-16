package ua.in.quireg.chan.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.support.v4.content.FileProvider;

import ua.in.quireg.chan.ui.activities.BrowserActivity;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.UriUtils;

import java.io.File;

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

        if (UriUtils.isImageUri(uri) ||
                (UriUtils.isVideoUri(uri) && Constants.SDK_VERSION >= 10)) {
            Intent browser = new Intent(context, BrowserActivity.class);
            browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            browser.setData(uri);
            context.startActivity(browser);
        } else {
            launchExternalBrowser(context, url);
        }
    }

    public static void playVideoExternal(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file), "video/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
}
