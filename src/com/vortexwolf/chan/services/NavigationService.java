package com.vortexwolf.chan.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.vortexwolf.chan.activities.BrowserActivity;
import com.vortexwolf.chan.activities.PickBoardActivity;
import com.vortexwolf.chan.activities.PostsListActivity;
import com.vortexwolf.chan.activities.ThreadsListActivity;
import com.vortexwolf.chan.boards.dvach.DvachUriParser;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.utils.UriUtils;

public class NavigationService {
    private final DvachUriParser mUriParser;

    public NavigationService(DvachUriParser uriParser) {
        this.mUriParser = uriParser;
    }

    public void navigate(Uri uri, Context context) {
        Bundle extras = new Bundle();
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        this.navigate(uri, context, extras, null);
    }

    public void navigate(Uri uri, Context context, Bundle extras, Integer flags) {
        Class c;
        if (this.mUriParser.isBoardUri(uri)) {
            c = ThreadsListActivity.class;
        } else if (this.mUriParser.isThreadUri(uri)) {
            c = PostsListActivity.class;
        } else if (UriUtils.isImageUri(uri)) {
            c = BrowserActivity.class;
        } else {
            c = PickBoardActivity.class;
        }

        this.navigateActivity(context, c, uri, extras, flags);
    }

    private void navigateActivity(Context context, Class<?> activityClass, Uri uri, Bundle extras, Integer flags) {
        Intent i = new Intent(context.getApplicationContext(), activityClass);
        i.setData(uri);
        i.putExtras(extras);
        if (flags != null) {
            i.addFlags(flags);
        }

        context.startActivity(i);
    }
}
