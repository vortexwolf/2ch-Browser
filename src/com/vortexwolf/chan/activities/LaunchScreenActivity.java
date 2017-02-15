package com.vortexwolf.chan.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class LaunchScreenActivity extends Activity {
    public static final String LOG_TAG = LaunchScreenActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent launchPickBoardActivity = new Intent(LaunchScreenActivity.this, PickBoardActivity.class);
        LaunchScreenActivity.this.startActivity(launchPickBoardActivity);
        finish();
    }
}
