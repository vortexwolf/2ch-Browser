package ua.in.quireg.chan.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import ua.in.quireg.chan.services.PermissionManager;

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
            launchMainActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        Intent baseActivity = new Intent(LaunchScreenActivity.this, BaseActivity.class);
        baseActivity.setAction(Intent.ACTION_MAIN);
        LaunchScreenActivity.this.startActivity(baseActivity);
        finish();
    }
}
