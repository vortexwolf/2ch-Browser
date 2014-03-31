package com.vortexwolf.chan.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.services.NavigationService;

public class ExternalUrlHandlerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationService navigationService = Factory.resolve(NavigationService.class);
        navigationService.navigate(this.getIntent().getData(), this.getApplicationContext(), null, Intent.FLAG_ACTIVITY_NEW_TASK);

        this.finish();
    }
}
