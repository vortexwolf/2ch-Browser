package ua.in.quireg.chan.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.ExtendedHttpClient;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.services.NavigationService;
import ua.in.quireg.chan.settings.ApplicationSettings;

import org.apache.http.impl.client.DefaultHttpClient;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    private ApplicationSettings mSettings;
    private SharedPreferences mSharedPreferences;
    private SharedPreferenceChangeListener mSharedPreferenceChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         mSettings = Factory.getContainer().resolve(ApplicationSettings.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(android.R.color.white));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.menu_preferences));
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if(getArguments() != null){
            rootKey = getArguments().getString("rootKey");
        }

        setPreferencesFromResource(R.xml.preferences, rootKey);

        this.mSharedPreferences = getDefaultSharedPreferences(getActivity().getApplicationContext());
        this.mSharedPreferenceChangeListener = new SharedPreferenceChangeListener();
        //TODO fix issue with wrong default values loaded on startup

        this.updateListSummary(R.string.pref_theme_key);
        this.updateListSummary(R.string.pref_text_size_key);
        this.updateListSummary(R.string.pref_image_preview_key);
        this.updateListSummary(R.string.pref_gif_preview_key);
        this.updateListSummary(R.string.pref_video_player_key);
        this.updateEditTextSummary(R.string.pref_cache_media_part_limit_key, R.string.loading);
        this.updateEditTextSummary(R.string.pref_cache_thumb_part_limit_key, R.string.loading);
        this.updateEditTextSummary(R.string.pref_cache_pages_part_limit_key, R.string.loading);
        this.updateEditTextSummary(R.string.pref_cache_pages_threshold_limit_key, R.string.loading);
        this.updateNameSummary();
        this.updateStartPageSummary();
        this.updateDownloadPathSummary();

    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString("rootKey", preferenceScreen.getKey());
        settingsFragment.setArguments(args);

        NavigationService.getInstance().pushFragment(settingsFragment);
    }

    @Override
    public void onResume() {
        super.onResume();

        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        this.mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
    }

    private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
            final Resources res = getActivity().getResources();
            final Preference preference = getPreferenceManager().findPreference(key);
            if(preference == null){
                return;
            }

            if (preference instanceof ListPreference) {
                updateListSummary(key);
            } else if (preference instanceof EditTextPreference) {
                preference.setSummary(((EditTextPreference) preference).getText());
            } else if (key.equals(res.getString(R.string.pref_name_key))) {
                updateNameSummary();
            } else if (key.equals(res.getString(R.string.pref_homepage_key))) {
                updateStartPageSummary();
            } else if (key.equals(res.getString(R.string.pref_download_path_key))) {
                updateDownloadPathSummary();
            } else if (key.equals(res.getString(R.string.pref_display_hidden_boards_key))) {

                final CheckBoxPreference pref = (CheckBoxPreference) preference;
                boolean isEnabled = mSharedPreferences.getBoolean(key, true);
                final SharedPreferences.Editor preferenceEditor = mSharedPreferences.edit();
                if (isEnabled) {
                    //Preference has already been changed to "true"
                    //Prepare rollback and show Warning dialog
                    preferenceEditor
                            .putBoolean(preference.getKey(), false);
                    new AlertDialog.Builder(getActivity())
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
        if(preference == null){
            return;
        }
        preference.setSummary(preference.getEntry());
    }

    private void updateEditTextSummary(int prefKey, int prefSummary) {
        EditTextPreference preference = (EditTextPreference) this.getPreferenceManager().findPreference(this.getString(prefKey));
        if(preference == null){
            return;
        }
        if (!StringUtils.isEmpty(preference.getText())) {
            preference.setSummary(preference.getText());
        } else {
            preference.setSummary(this.getString(prefSummary));
        }
    }
}


