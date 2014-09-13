package com.vortexwolf.chan.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
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
        this.setTheme(Constants.SDK_VERSION > 13 || Constants.SDK_VERSION < 6 ? this.mSettings.getTheme() : R.style.Theme_Dark_Medium);
        
        // create
        super.onCreate(savedInstanceState);

        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        this.mSharedPreferenceChangeListener = new SharedPreferenceChangeListener();

        this.addPreferencesFromResource(R.xml.preferences);

        this.updateListSummary(R.string.pref_video_preview_key);
        this.updateListSummary(R.string.pref_theme_key);
        this.updateListSummary(R.string.pref_text_size_key);
        this.updateListSummary(R.string.pref_image_preview_key);
        this.updateListSummary(R.string.pref_gif_preview_key);
        this.updateNameSummary();
        this.updateStartPageSummary();
        this.updateDownloadPathSummary();
        /*if (Constants.SDK_VERSION < 19) {
            this.disablePreference(R.string.pref_kitkat_fix_key);
        }*/
        if (Constants.SDK_VERSION < 11) {
            this.disablePreference(R.string.pref_disable_zoom_controls_key);
        }

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
            if (key.equals(res.getString(R.string.pref_unsafe_ssl_key))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationPreferencesActivity.this);
                builder.setMessage(R.string.notification_will_restart);
                builder.setCancelable(false);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);                 
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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
    
    private void disablePreference(int prefKey) {
        Preference preference = this.getPreferenceManager().findPreference(this.getString(prefKey));
        preference.setShouldDisableView(true);
        preference.setEnabled(false);
    }
}
