package com.vortexwolf.chan.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.services.PermissionManager;
import com.vortexwolf.chan.settings.ApplicationPreferencesActivity;

public class LaunchScreenActivity extends Activity {
    public static final String LOG_TAG = LaunchScreenActivity.class.getSimpleName();

    boolean storagePermissionRequestCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean result = PermissionManager.verifyPermissions(this);

        if(!result){
            PermissionManager.requestPermissions(this);
            //user has been prompted to grant permissions.
            //Waiting for onRequestPermissionsResult()
        }else{
            //no permissions grant required, proceed to main activity with minor delay
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            launchMainActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PermissionManager.REQUEST_EXTERNAL_STORAGE){
            storagePermissionRequestCompleted = true;
        }
        //In case more than one permission is required simply add another boolean value.
        // After all values are set to "true" main activity will be launched.
        if(storagePermissionRequestCompleted){
            launchMainActivity();
        }
    }

    private void launchMainActivity(){
        Intent launchPickBoardActivity = new Intent(LaunchScreenActivity.this, PickBoardActivity.class);
        launchPickBoardActivity.setAction(Intent.ACTION_MAIN);
        LaunchScreenActivity.this.startActivity(launchPickBoardActivity);
        finish();
    }
}
