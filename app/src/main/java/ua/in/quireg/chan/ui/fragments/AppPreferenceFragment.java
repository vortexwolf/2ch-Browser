package ua.in.quireg.chan.ui.fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;
import ua.in.quireg.chan.BuildConfig;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.services.NavigationService;

public class AppPreferenceFragment extends PreferenceFragmentCompat {

    @Inject protected MainApplication mMainApplication;
    @Inject protected SharedPreferences mSharedPreferences;

    private SharedPreferenceChangeListener mSharedPreferenceChangeListener;

    private boolean mNetworkConfigChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainApplication.getComponent().inject(this);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.menu_preferences));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (getArguments() != null) {
            rootKey = getArguments().getString("rootKey");
        }
        setPreferencesFromResource(R.xml.preferences, rootKey);

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
        updateEditTextSummary(R.string.pref_proxy_address_key);
        updateEditTextSummary(R.string.pref_proxy_port_key);
        updateEditTextSummary(R.string.pref_proxy_auth_login_key);
        updateProxyPassSummary();
        updateNameSummary();
        updateStartPageSummary();
        updateDownloadPathSummary();
        updateAppVersionPreference();

    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        AppPreferenceFragment appPreferenceFragment = new AppPreferenceFragment();
        Bundle args = new Bundle();
        args.putString("rootKey", preferenceScreen.getKey());
        appPreferenceFragment.setArguments(args);

        NavigationService.getInstance().pushFragment(appPreferenceFragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.v("onStart()");

        mNetworkConfigChanged = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.v("onStop()");

        if (mNetworkConfigChanged) {
            Timber.d("network config changed");
            boolean success = verifyNetworkParameters();
            if (!success) {
                disableProxyConfig();
                Timber.w("cannot apply new proxy settings, proxy disabled");
            }
            mMainApplication.rebuildAppComponent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.v("onResume()");

        mSharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.v("onPause()");

        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
            final Preference preference = getPreferenceManager().findPreference(key);

            preference.setShouldDisableView(!preference.isEnabled());

            if (preference instanceof ListPreference) {
                preference.setSummary(((ListPreference) preference).getEntry());
            } else if (preference instanceof EditTextPreference) {
                preference.setSummary(((EditTextPreference) preference).getText());
            }

            if (key.equals(getString(R.string.pref_name_key))) {
                updateNameSummary();
            } else if (key.equals(getString(R.string.pref_homepage_key))) {
                updateStartPageSummary();
            } else if (key.equals(getString(R.string.pref_download_path_key))) {
                updateDownloadPathSummary();
            } else if (key.equals(getString(R.string.pref_display_hidden_boards_key))) {
                showFullBoardsListWarning(preference);
            } else if (key.equals(getString(R.string.pref_unsafe_ssl_key)) ||
                    key.equals(getString(R.string.pref_use_proxy_key)) ||
                    key.equals(getString(R.string.pref_proxy_address_key)) ||
                    key.equals(getString(R.string.pref_proxy_port_key)) ||
                    key.equals(getString(R.string.pref_proxy_auth_key)) ||
                    key.equals(getString(R.string.pref_proxy_auth_login_key)) ||
                    key.equals(getString(R.string.pref_proxy_auth_pass_key))
                    ) {
                mNetworkConfigChanged = true;
                if (key.equals(getString(R.string.pref_proxy_auth_pass_key))) {
                    updateProxyPassSummary();
                }
            }
        }
    }

    private void updateAppVersionPreference() {
        try {
            EditTextPreference preference = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.pref_screen_about_version_key));
            if (preference == null) {
                return;
            }
            PackageInfo pinfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String versionName = pinfo.versionName;
            if (!versionName.isEmpty()) {
                preference.setSummary(String.format(Locale.getDefault(),
                        "Version: %s-%s, SDK %s, %s",
                        versionName, BuildConfig.BUILD_TYPE, Constants.SDK_VERSION, new Date(BuildConfig.BUILD_TIMESTAMP).toString()
                ));
            }

        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e.getMessage());
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
        ListPreference preference = (ListPreference) getPreferenceManager().findPreference(getString(prefKeyId));
        if (preference != null) {
            preference.setSummary(preference.getEntry());
        }
    }

    private void updateEditTextSummary(int prefKey) {
        EditTextPreference preference = (EditTextPreference) getPreferenceManager().findPreference(getString(prefKey));
        if (preference == null) {
            return;
        }
        if (!StringUtils.isEmpty(preference.getText())) {
            preference.setSummary(preference.getText());
        }
    }

    private void updateEditTextSummary(int prefKey, int prefSummary) {
        EditTextPreference preference = (EditTextPreference) getPreferenceManager().findPreference(getString(prefKey));
        if (preference == null) {
            return;
        }
        if (!StringUtils.isEmpty(preference.getText())) {
            preference.setSummary(preference.getText());
        } else {
            preference.setSummary(getString(prefSummary));
        }
    }

    private void updateProxyPassSummary() {
        EditTextPreference preference = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.pref_proxy_auth_pass_key));
        if (preference == null) {
            return;
        }
        if (!StringUtils.isEmpty(preference.getText())) {
            preference.setSummary(getString(R.string.password_string));
        } else {
            preference.setSummary(getString(R.string.pref_proxy_auth_pass_summary));
        }
    }

    private void showFullBoardsListWarning(Preference preference) {
        final CheckBoxPreference pref = (CheckBoxPreference) preference;

        boolean isEnabled = mSharedPreferences.getBoolean(preference.getKey(), true);

        final SharedPreferences.Editor preferenceEditor = mSharedPreferences.edit();
        if (isEnabled) {
            //Preference has already been changed to "true"
            //Prepare rollback and show Warning dialog
            preferenceEditor
                    .putBoolean(preference.getKey(), false);
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.unmoderated_boards_list_popup_title))
                    .setMessage(getString(R.string.unmoderated_boards_list_popup_text))
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

    private boolean verifyNetworkParameters() {

        if (mSharedPreferences.getBoolean(getString(R.string.pref_use_proxy_key), true)) {

            String proxyAddress = mSharedPreferences.getString(getString(R.string.pref_proxy_address_key), "");
            if (StringUtils.isEmpty(proxyAddress)) {
                AppearanceUtils.showLongToast(getContext(), "Proxy address cannot be empty");
                return false;
            }

            String proxyPort = mSharedPreferences.getString(getString(R.string.pref_proxy_port_key), "");
            if (!isValidPort(proxyPort)) {
                AppearanceUtils.showLongToast(getContext(), "Invalid proxy port.\n Must be between 0 and 65535");
                return false;
            }
        }

        if (mSharedPreferences.getBoolean(getString(R.string.pref_proxy_auth_key), true)) {

            String proxyLogin = mSharedPreferences.getString(getString(R.string.pref_proxy_auth_login_key), "");
            if (StringUtils.isEmpty(proxyLogin)) {
                AppearanceUtils.showLongToast(getContext(), "Proxy login cannot be empty");
                return false;
            }

            String proxyPassword = mSharedPreferences.getString(getString(R.string.pref_proxy_auth_pass_key), "");
            if (StringUtils.isEmpty(proxyPassword)) {
                AppearanceUtils.showLongToast(getContext(), "Proxy password cannot be empty");
                return false;
            }

        }

        return true;
    }

    private boolean isValidPort(String proxyPort) {
        if (StringUtils.isEmpty(proxyPort)) {
            return false;
        }
        try {
            int proxyPortInt = Integer.parseInt(proxyPort);
            if (proxyPortInt < 0 || proxyPortInt > 65535) {
                return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private void disableProxyConfig() {
        mSharedPreferences.edit().putBoolean(getString(R.string.pref_use_proxy_key), false).apply();
        mSharedPreferences.edit().putBoolean(getString(R.string.pref_proxy_auth_key), false).apply();
    }

}


