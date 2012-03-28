package com.vortexwolf.dvach.settings;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.library.Tracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ApplicationPreferencesActivity extends PreferenceActivity {

	private ApplicationSettings mSettings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MainApplication application = (MainApplication)this.getApplication();
		mSettings = application.getSettings();

		addPreferencesFromResource(R.xml.preferences);

        Tracker.getInstance().trackActivityView("ApplicationPreferencesActivity");
	}

	@Override
	protected void onResume() {
	    super.onResume();

	    mSettings.startTrackChanges();
	}

	@Override
	protected void onPause() {
	    super.onPause();

	    mSettings.stopTrackChanges();
	}
}
