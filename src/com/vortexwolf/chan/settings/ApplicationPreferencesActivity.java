package com.vortexwolf.chan.settings;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.MainApplication;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.services.MyTracker;

public class ApplicationPreferencesActivity extends PreferenceActivity {
    private static final String TAG = "ApplicationPreferencesActivity";

    private ApplicationSettings mSettings;
    private SharedPreferences mSharedPreferences;
    private SharedPreferenceChangeListener mSharedPreferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set theme before creating
        this.mSettings = Factory.resolve(ApplicationSettings.class);
        this.setTheme(this.mSettings.getTheme()); 
        
        // create
        super.onCreate(savedInstanceState);

        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        this.mSharedPreferenceChangeListener = new SharedPreferenceChangeListener();

        this.addPreferencesFromResource(R.xml.preferences);

        this.updateListSummary(R.string.pref_video_preview_key);
        this.updateListSummary(R.string.pref_theme_key);
        this.updateListSummary(R.string.pref_text_size_key);
        this.updateNameSummary();
        this.updateStartPageSummary();
        this.updateDownloadPathSummary();

        Factory.getContainer().resolve(MyTracker.class).trackActivityView(TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
    }

    private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Resources res = ApplicationPreferencesActivity.this.getResources();
            Preference preference = ApplicationPreferencesActivity.this.getPreferenceManager().findPreference(key);

            if (preference instanceof ListPreference) {
                ApplicationPreferencesActivity.this.updateListSummary(key);
            } else if (key.equals(res.getString(R.string.pref_name_key))) {
                ApplicationPreferencesActivity.this.updateNameSummary();
            } else if (key.equals(res.getString(R.string.pref_homepage_key))) {
                ApplicationPreferencesActivity.this.updateStartPageSummary();
            } else if (key.equals(res.getString(R.string.pref_download_path_key))) {
                ApplicationPreferencesActivity.this.updateDownloadPathSummary();
            }
        }
    }

    private void updateNameSummary() {
        this.updateEditTextSummary(R.string.pref_name_key, R.string.pref_name_summary);
    }

    private void updateStartPageSummary() {
        this.updateEditTextSummary(R.string.pref_homepage_key, R.string.pref_homepage_summary);
    }

    private void updateDownloadPathSummary() {
        this.updateEditTextSummary(R.string.pref_download_path_key, R.string.pref_download_path_summary);
    }

    private void updateListSummary(int prefKeyId) {
        this.updateListSummary(this.getString(prefKeyId));
    }

    private void updateListSummary(String prefKey) {
        ListPreference preference = (ListPreference) this.getPreferenceManager().findPreference(prefKey);
        preference.setSummary(preference.getEntry());
    }

    private void updateEditTextSummary(int prefKey, int prefSummary) {
        EditTextPreference preference = (EditTextPreference) this.getPreferenceManager().findPreference(this.getString(prefKey));
        if (!StringUtils.isEmpty(preference.getText())) {
            preference.setSummary(preference.getText());
        } else {
            preference.setSummary(this.getString(prefSummary));
        }
    }
}
