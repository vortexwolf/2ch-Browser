package com.vortexwolf.dvach.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.vortexwolf.dvach.activities.PostsListActivity;
import com.vortexwolf.dvach.activities.ThreadsListActivity;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.INavigationService;

public class NavigationService implements INavigationService {

    public NavigationService() {
    }

    @Override
    public void navigate(Uri uri, Context context) {
        Bundle extras = new Bundle();
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        this.navigate(uri, context, extras);
    }

    @Override
    public void navigate(Uri uri, Context context, Bundle extras) {
        if (UriUtils.isBoardUri(uri)) {
            this.navigateActivity(context, ThreadsListActivity.class, uri, extras);
        } else if (UriUtils.isThreadUri(uri)) {
            this.navigateActivity(context, PostsListActivity.class, uri, extras);
        }
    }

    private void navigateActivity(Context context, Class<?> activityClass, Uri uri, Bundle extras) {
        Intent i = new Intent(context.getApplicationContext(), activityClass);
        i.setData(uri);
        i.putExtras(extras);
        context.startActivity(i);
    }
}
