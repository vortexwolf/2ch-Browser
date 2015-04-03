package com.vortexwolf.chan.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.vortexwolf.chan.activities.BrowserActivity;
import com.vortexwolf.chan.activities.PickBoardActivity;
import com.vortexwolf.chan.activities.PostsListActivity;
import com.vortexwolf.chan.activities.ThreadsListActivity;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.interfaces.IUrlParser;
import com.vortexwolf.chan.interfaces.IWebsite;

public class NavigationService {

    public NavigationService() {
    }

    public void navigate(Uri uri, Context context, Integer flags, boolean preferDeserialized) {
        IWebsite website = Websites.fromUri(uri);
        IUrlParser urlParser = website.getUrlParser();

        if (urlParser.isBoardUri(uri)) {
            this.navigateBoardPage(context, null, website.name(), urlParser.getBoardName(uri), urlParser.getBoardPageNumber(uri), preferDeserialized);
        } else if (urlParser.isThreadUri(uri)) {
            this.navigateThread(context, null, website.name(), urlParser.getBoardName(uri), urlParser.getThreadNumber(uri), null, urlParser.getPostNumber(uri), preferDeserialized);
        } else if (UriUtils.isImageUri(uri) || UriUtils.isWebmUri(uri)) {
            this.navigateActivity(context, BrowserActivity.class, uri, null, flags);
        } else {
            this.navigateBoardList(context, website.name(), false);
        }
    }

    public void navigateBoardPage(Context context, Bundle extras, String website, String board, int page, boolean preferDeserialized) {
        extras = extras != null ? extras : new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putInt(Constants.EXTRA_BOARD_PAGE, page);
        if (preferDeserialized) {
            extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);
        }

        this.navigateActivity(context, ThreadsListActivity.class, null, extras, null);
    }

    public void navigateThread(Context context, Bundle extras, String website, String board, String thread, String subject, String post, boolean preferDeserialized) {
        extras = extras != null ? extras : new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putString(Constants.EXTRA_THREAD_NUMBER, thread);
        extras.putString(Constants.EXTRA_THREAD_SUBJECT, subject);
        extras.putString(Constants.EXTRA_POST_NUMBER, post);
        if (preferDeserialized) {
            extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);
        }

        this.navigateActivity(context, PostsListActivity.class, null, extras, null);
    }

    public void navigateBoardList(Context context, String website, boolean flagNoHistory) {
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);

        Integer flags = flagNoHistory ? Intent.FLAG_ACTIVITY_NO_HISTORY : null;

        this.navigateActivity(context, PickBoardActivity.class, null, extras, flags);
    }

    public void navigateCatalog(Context context, String website, String board) {
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_CATALOG, true);

        this.navigateActivity(context, ThreadsListActivity.class, null, extras, null);
    }

    private void navigateActivity(Context context, Class<?> activityClass, Uri data, Bundle extras, Integer flags) {
        Intent i = new Intent(context.getApplicationContext(), activityClass);
        if (data != null) {
            i.setData(data);
        }
        if (extras != null) {
            i.putExtras(extras);
        }
        if (flags != null) {
            i.addFlags(flags);
        }

        context.startActivity(i);
    }
}
