package com.vortexwolf.dvach.settings;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.presentation.services.Tracker;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ApplicationPreferencesActivity extends PreferenceActivity {

	private ApplicationSettings mSettings;
	private Resources mResources;
	private SharedPreferences mSharedPreferences;
	private SharedPreferenceChangeListener mSharedPreferenceChangeListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MainApplication application = (MainApplication)this.getApplication();
		mSettings = application.getSettings();
		mResources = application.getResources();
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		mSharedPreferenceChangeListener = new SharedPreferenceChangeListener();
		
		addPreferencesFromResource(R.xml.preferences);

		mSharedPreferenceChangeListener.onSharedPreferenceChanged(mSharedPreferences, mResources.getString(R.string.pref_theme_key));
		mSharedPreferenceChangeListener.onSharedPreferenceChanged(mSharedPreferences, mResources.getString(R.string.pref_text_size_key));
		
        Tracker.getInstance().trackActivityView("ApplicationPreferencesActivity");
	}

	@Override
	protected void onResume() {
	    super.onResume();

	    mSharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
	    mSettings.startTrackChanges();
	}

	@Override
	protected void onPause() {
	    super.onPause();

	    mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);	
	    mSettings.stopTrackChanges();
	}
	
	private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener{
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if(key.equals(mResources.getString(R.string.pref_theme_key)) 
				|| key.equals(mResources.getString(R.string.pref_text_size_key)) ){
				
				ListPreference preference = (ListPreference)getPreferenceManager().findPreference(key);
				preference.setSummary(preference.getEntry());
			}

		}
	}
}
