package com.vortexwolf.chan.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.utils.StringUtils;

import org.apache.http.impl.client.DefaultHttpClient;

public class ApplicationPreferencesActivity extends PreferenceActivity {
    private static final String TAG = ApplicationPreferencesActivity.class.getSimpleName();

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

        this.addPreferencesFromResource(R.xml.preferences);

        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        this.mSharedPreferenceChangeListener = new SharedPreferenceChangeListener();
        //TODO fix issue with wrong default values loaded on startup

        this.updateListSummary(R.string.pref_theme_key);
        this.updateListSummary(R.string.pref_text_size_key);
        this.updateListSummary(R.string.pref_image_preview_key);
        this.updateListSummary(R.string.pref_gif_preview_key);
        this.updateListSummary(R.string.pref_video_player_key);
        this.updateListSummary(R.string.pref_captcha_type_key);
        this.updateEditTextSummary(R.string.pref_cache_media_part_limit_key, R.string.loading);
        this.updateEditTextSummary(R.string.pref_cache_thumb_part_limit_key, R.string.loading);
        this.updateEditTextSummary(R.string.pref_cache_pages_part_limit_key, R.string.loading);
        this.updateEditTextSummary(R.string.pref_cache_pages_threshold_limit_key, R.string.loading);
        this.updateNameSummary();
        this.updateStartPageSummary();
        this.updateDownloadPathSummary();
        if (Constants.SDK_VERSION < 11) {
            this.disablePreference(R.string.pref_disable_zoom_controls_key);
        }
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
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
            final Resources res = ApplicationPreferencesActivity.this.getResources();
            final Preference preference = ApplicationPreferencesActivity.this.getPreferenceManager().findPreference(key);

            if (preference instanceof ListPreference) {
                ApplicationPreferencesActivity.this.updateListSummary(key);
            } else if (preference instanceof EditTextPreference) {
                preference.setSummary(((EditTextPreference) preference).getText());
            } else if (key.equals(res.getString(R.string.pref_name_key))) {
                ApplicationPreferencesActivity.this.updateNameSummary();
            } else if (key.equals(res.getString(R.string.pref_homepage_key))) {
                ApplicationPreferencesActivity.this.updateStartPageSummary();
            } else if (key.equals(res.getString(R.string.pref_download_path_key))) {
                ApplicationPreferencesActivity.this.updateDownloadPathSummary();
            } else if (key.equals(res.getString(R.string.pref_display_hidden_boards_key))) {

                final CheckBoxPreference pref = (CheckBoxPreference) preference;
                boolean isEnabled = mSharedPreferences.getBoolean(key, true);
                final SharedPreferences.Editor preferenceEditor = mSharedPreferences.edit();
                if (isEnabled) {
                    //Preference has already been changed to "true"
                    //Prepare rollback and show Warning dialog
                    preferenceEditor
                            .putBoolean(preference.getKey(), false);
                    new AlertDialog.Builder(ApplicationPreferencesActivity.this)
                            .setTitle(res.getString(R.string.unmoderated_boards_list_popup_title))
                            .setMessage(res.getString(R.string.unmoderated_boards_list_popup_text))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //leave it as it is
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //rollback
                                    preferenceEditor.apply();
                                    pref.setChecked(false);
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    //rollback
                                    preferenceEditor.apply();
                                    pref.setChecked(false);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            }
            if (key.equals(res.getString(R.string.pref_unsafe_ssl_key))) {
                ((ExtendedHttpClient) Factory.resolve(DefaultHttpClient.class)).setSafe(!mSettings.isUnsafeSSL());
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
