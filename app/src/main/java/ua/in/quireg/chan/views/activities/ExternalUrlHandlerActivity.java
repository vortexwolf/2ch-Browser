package ua.in.quireg.chan.views.activities;

import android.app.Activity;
import android.os.Bundle;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.services.NavigationService;

public class ExternalUrlHandlerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationService navigationService = Factory.resolve(NavigationService.class);
        //TODO fix this
        //navigationService.navigate(this.getIntent().getData(), this, Intent.FLAG_ACTIVITY_NEW_TASK, false);

        this.finish();
    }
}
