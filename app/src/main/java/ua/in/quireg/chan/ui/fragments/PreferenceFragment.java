package ua.in.quireg.chan.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
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

public class PreferenceFragment extends PreferenceFragmentCompat {
    private static final String LOG_TAG = PreferenceFragment.class.getSimpleName();

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

        mSharedPreferences = getDefaultSharedPreferences(getActivity().getApplicationContext());
        mSharedPreferenceChangeListener = new SharedPreferenceChangeListener();

        updateListSummary(R.string.pref_theme_key);
        updateListSummary(R.string.pref_text_size_key);
        updateListSummary(R.string.pref_image_preview_key);
        updateListSummary(R.string.pref_gif_preview_key);
        updateListSummary(R.string.pref_video_player_key);
        updateEditTextSummary(R.string.pref_cache_media_part_limit_key, R.string.loading);
        updateEditTextSummary(R.string.pref_cache_thumb_part_limit_key, R.string.loading);
        updateEditTextSummary(R.string.pref_cache_pages_part_limit_key, R.string.loading);
        updateEditTextSummary(R.string.pref_cache_pages_threshold_limit_key, R.string.loading);
        updateNameSummary();
        updateStartPageSummary();
        updateDownloadPathSummary();
        updateAppVersionPreference();

    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        PreferenceFragment preferenceFragment = new PreferenceFragment();
        Bundle args = new Bundle();
        args.putString("rootKey", preferenceScreen.getKey());
        preferenceFragment.setArguments(args);

        NavigationService.getInstance().pushFragment(preferenceFragment);
    }

    @Override
    public void onResume() {
        super.onResume();

        mSharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
            final Preference preference = getPreferenceManager().findPreference(key);
            if(preference == null){
                return;
            }

            final Resources res = getActivity().getResources();

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
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                //leave it as it is
                            })
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {
                                //rollback
                                preferenceEditor.apply();
                                pref.setChecked(false);
                            })
                            .setOnCancelListener(dialogInterface -> {
                                //rollback
                                preferenceEditor.apply();
                                pref.setChecked(false);
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
    private void updateAppVersionPreference() {
        try {
            EditTextPreference preference = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.pref_screen_about_version_key));
            if(preference == null){
                return;
            }
            PackageInfo pinfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String versionName = pinfo.versionName;
            if(!versionName.isEmpty()){
                preference.setSummary(versionName);
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    private void updateNameSummary() {
        updateEditTextSummary(R.string.pref_name_key, R.string.pref_name_summary);
    }

    private void updateStartPageSummary() {
        updateEditTextSummary(R.string.pref_homepage_key, R.string.pref_homepage_summary);
    }

    private void updateDownloadPathSummary() {
        updateEditTextSummary(R.string.pref_download_path_key, R.string.pref_download_path_summary);
    }

    private void updateListSummary(int prefKeyId) {
        updateListSummary(getString(prefKeyId));
    }

    private void updateListSummary(String prefKey) {
        ListPreference preference = (ListPreference) getPreferenceManager().findPreference(prefKey);
        if(preference == null){
            return;
        }
        preference.setSummary(preference.getEntry());
    }

    private void updateEditTextSummary(int prefKey, int prefSummary) {
        EditTextPreference preference = (EditTextPreference) getPreferenceManager().findPreference(getString(prefKey));
        if(preference == null){
            return;
        }
        if (!StringUtils.isEmpty(preference.getText())) {
            preference.setSummary(preference.getText());
        } else {
            preference.setSummary(getString(prefSummary));
        }
    }
}


