package com.vortexwolf.dvach.activities;

import com.vortexwolf.dvach.common.utils.UriUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class ExternalUrlHandlerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Uri uri = this.getIntent().getData();
        Context context = this.getApplicationContext();
        Intent intent = null;
        if (UriUtils.isBoardUri(uri)) {
            intent = new Intent(context, ThreadsListActivity.class);
        } else if (UriUtils.isThreadUri(uri)) {
            intent = new Intent(context, PostsListActivity.class);
        } else if (UriUtils.isImageUri(uri)) {
            intent = new Intent(context, BrowserActivity.class);
        } else {
            intent = new Intent(context, PickBoardActivity.class);
        }
        
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        this.finish();
    }
}
