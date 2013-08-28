package com.vortexwolf.dvach.activities;

import com.vortexwolf.dvach.common.utils.UriUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class ExternalUrlHandlerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Uri uri = this.getIntent().getData();
        Intent intent = null;
        if (UriUtils.isBoardUri(uri)) {
            intent = new Intent(this, ThreadsListActivity.class);
        } else if (UriUtils.isThreadUri(uri)) {
            intent = new Intent(this, PostsListActivity.class);
        } else if (UriUtils.isImageUri(uri)) {
            intent = new Intent(this, BrowserActivity.class);
        } else {
            intent = new Intent(this, PickBoardActivity.class);
        }
        
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        this.finish();
    }

}
