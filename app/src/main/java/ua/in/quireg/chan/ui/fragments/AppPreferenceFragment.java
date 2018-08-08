package ua.in.quireg.chan.ui.fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.settings.SeekBarDialogPreference;
import ua.in.quireg.chan.settings.SeekBarDialogPreferenceFragment;
import ua.in.quireg.chan.ui.views.AppPreferencesDividerItemDecoration;

public class AppPreferenceFragment extends PreferenceFragmentCompat {

    @Inject protected SharedPreferences mSharedPreferences;
    @Inject protected MainRouter mRouter;

    private SharedPreferenceChangeListener mSharedPreferenceChangeListener = new SharedPreferenceChangeListener();

    private boolean mNetworkConfigChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (getArguments() != null) {
            rootKey = getArguments().getString("rootKey");
        }
        setPreferencesFromResource(R.xml.preferences, rootKey);

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

        updateProxyAddressSummary();
        updateProxyPortSummary();
        updateProxyLoginSummary();
        updateProxyPassSummary();

        updateAppVersion();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set proper background according to applied theme
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();

        theme.resolveAttribute(R.attr.activityRootBackground, typedValue, true);
        @ColorInt int color = typedValue.data;

        view.setBackgroundColor(color);

        //Remove builtin item decorator and replace with custom.
        RecyclerView recyclerView = view.findViewById(R.id.list);

        int c = recyclerView.getItemDecorationCount();
        for (int i = 0; i < c; i++) {
            recyclerView.removeItemDecorationAt(i);
        }
        recyclerView.addItemDecoration(new AppPreferencesDividerItemDecoration(getContext()));


    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof SeekBarDialogPreference) {
            DialogFragment dialogFragment = SeekBarDialogPreferenceFragment.newInstance(preference);
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        AppPreferenceFragment appPreferenceFragment = new AppPreferenceFragment();
        Bundle args = new Bundle();
        args.putString("rootKey", preferenceScreen.getKey());
        appPreferenceFragment.setArguments(args);
        mRouter.pushFragment(appPreferenceFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            mRouter.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.menu_preferences));

    }

    @Override
    public void onStop() {
        super.onStop();
        restartNetworkingIfChanged();
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

            //Disable preference view if needed
            preference.setShouldDisableView(!preference.isEnabled());

            if (preference instanceof ListPreference) {

                //Update any ListPreference summary with it's entry name
                preference.setSummary(((ListPreference) preference).getEntry());

            } else if (preference instanceof EditTextPreference) {

                //Update any EditTextPreference summary with it's value
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

            } else if (isNetworkPreference(key)) {

                updateProxyAddressSummary();
                updateProxyPortSummary();
                updateProxyLoginSummary();
                updateProxyPassSummary();

                mNetworkConfigChanged = true;

            } else if (key.equals(getString(R.string.pref_theme_key)) || key.equals(getString(R.string.pref_text_size_key))) {

                getActivity().recreate();

            }
        }
    }

    private void updateAppVersion() {
        try {
            EditTextPreference preference = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.pref_screen_about_version_key));
            if (preference == null) {
                return;
            }
            PackageInfo pinfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String versionName = pinfo.versionName;
            if (!versionName.isEmpty()) {
                preference.setSummary(String.format(Locale.getDefault(),
                        "Version: %s-alpha-%s, SDK %s, %s",
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

    private void updateProxyAddressSummary() {
        updateEditTextSummary(R.string.pref_proxy_address_key, R.string.pref_proxy_address_summary);
    }

    private void updateProxyPortSummary() {
        updateEditTextSummary(R.string.pref_proxy_port_key, R.string.pref_proxy_port_summary);
    }

    private void updateProxyLoginSummary() {
        updateEditTextSummary(R.string.pref_proxy_auth_login_key, R.string.pref_proxy_auth_login_summary);
    }

    private void updateProxyPassSummary() {
        updateEditTextSummary(R.string.pref_proxy_auth_pass_key, R.string.pref_proxy_auth_pass_summary);

        EditTextPreference preference = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.pref_proxy_auth_pass_key));
        if (preference == null) {
            return;
        }
        String value = preference.getText();
        if (!StringUtils.isEmpty(value)) {
            String mask = new String(new char[value.length()]).replace("\0", "*");
            ;
            preference.setSummary(mask);
        } else {
            preference.setSummary(getString(R.string.pref_proxy_auth_pass_summary));
        }
    }

    private void updateListSummary(int prefKeyId) {
        ListPreference preference = (ListPreference) getPreferenceManager().findPreference(getString(prefKeyId));
        if (preference != null) {
            preference.setSummary(preference.getEntry());
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

    private void showFullBoardsListWarning(Preference preference) {
        final CheckBoxPreference pref = (CheckBoxPreference) preference;

        boolean isEnabled = mSharedPreferences.getBoolean(preference.getKey(), true);

        final SharedPreferences.Editor preferenceEditor = mSharedPreferences.edit();
        if (isEnabled) {

            //Preference has already been changed to "true"
            //Prepare rollback and show Warning dialog
            preferenceEditor
                    .putBoolean(preference.getKey(), false);

            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.warning_popup_title))
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
                    .setIcon(R.drawable.browser_logo)
                    .show();

            TextView messageView = alertDialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.FILL);
        }
    }

    private boolean verifyProxyConfig() {
        //TODO add strings to general file and add translations

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

    private boolean isNetworkPreference(String key) {
        return key.equals(getString(R.string.pref_unsafe_ssl_key)) ||
                key.equals(getString(R.string.pref_use_proxy_key)) ||
                key.equals(getString(R.string.pref_proxy_address_key)) ||
                key.equals(getString(R.string.pref_proxy_port_key)) ||
                key.equals(getString(R.string.pref_proxy_auth_key)) ||
                key.equals(getString(R.string.pref_proxy_auth_login_key)) ||
                key.equals(getString(R.string.pref_proxy_auth_pass_key)) ||
                key.equals(getString(R.string.pref_use_https_key));
    }

    private void restartNetworkingIfChanged() {

        if (!verifyProxyConfig()) {
            Timber.w("cannot apply new proxy settings, proxy disabled");

            //Disable proxy and proxy auth
            mSharedPreferences.edit().putBoolean(getString(R.string.pref_use_proxy_key), false).apply();
            mSharedPreferences.edit().putBoolean(getString(R.string.pref_proxy_auth_key), false).apply();
        }

        if (mNetworkConfigChanged) {
            Timber.d("network config changed");

            mNetworkConfigChanged = false;
            getActivity().recreate();
        }
    }
}


