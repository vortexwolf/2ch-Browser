package com.vortexwolf.dvach.settings;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.asynctasks.CheckPasscodeTask;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;

public class ApplicationSettings implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "ApplicationSettings";

    private final Context mContext;
    private final SharedPreferences mSettings;
    private final Resources mResources;

    public ApplicationSettings(Context context, Resources resources) {
        this.mContext = context;
        this.mSettings = PreferenceManager.getDefaultSharedPreferences(context);
        this.mResources = resources;
    }

    public void startTrackChanges() {
        this.mSettings.registerOnSharedPreferenceChangeListener(this);
    }

    public void stopTrackChanges() {
        this.mSettings.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(this.mResources.getString(R.string.pref_passcode_key))) {
            String passcode = this.getPasscode();
            this.sendPasscodeToServer(passcode);
        }
    }

    private void sendPasscodeToServer(String passcode) {
        final Uri uri = Uri.withAppendedPath(this.getDomainUri(), "makaba/makaba.fcgi");

        CheckPasscodeTask task = new CheckPasscodeTask(this.mContext, uri, passcode);
        task.execute();
    }

    public String getHomepage() {
        String boardName = this.mSettings.getString(this.mResources.getString(R.string.pref_homepage_key), Constants.DEFAULT_BOARD).toLowerCase();
        return !StringUtils.isEmpty(boardName) ? boardName : Constants.DEFAULT_BOARD;
    }

    public String getDownloadPath() {
        String path = this.mSettings.getString(this.mResources.getString(R.string.pref_download_path_key), null);

        return !StringUtils.isEmptyOrWhiteSpace(path) ? path : Constants.DEFAULT_DOWNLOAD_FOLDER;
    }

    public String getName() {
        return this.mSettings.getString(this.mResources.getString(R.string.pref_name_key), null);
    }

    public String getPasscode() {
        return this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_key), null);
    }

    public Uri getDomainUri() {
        String domain = this.mSettings.getString(this.mResources.getString(R.string.pref_domain_key), null);
        domain = StringUtils.isEmpty(domain) ? Constants.DEFAULT_DOMAIN : domain;
        Uri uri = UriUtils.getUriForDomain(domain);

        return uri;
    }
    
    public int getLongPostsMaxHeight() {
        String maxHeightStr = this.mSettings.getString(this.mResources.getString(R.string.pref_cut_posts_key), null);
        int defaultValue = 400;
        
        if (maxHeightStr == null) {
            return defaultValue;
        }
        
        try {
            int maxHeight = Integer.parseInt(maxHeightStr);
            return maxHeight;
        } catch(NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean isLocalDateTime() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_convert_post_date_key), true);
    }

    public boolean isDownloadInBackground() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_download_background_key), true);
    }

    public boolean isLoadThumbnails() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_load_thumbnails_key), true);
    }

    public boolean isDisplayPostItemDate() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_display_post_date_key), false);
    }

    public boolean isLinksInPopup() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_popup_link_key), true);
    }

    public boolean isDisplayNavigationBar() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_display_navigation_bar_key), true);
    }

    public boolean isFileCacheEnabled() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_file_cache_key), true);
    }

    public boolean isFileCacheSdCard() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_file_cache_sdcard_key), true);
    }

    public boolean isAutoRefresh() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_auto_refresh_key), false);
    }

    public int getAutoRefreshInterval() {
        return this.mSettings.getInt(this.mResources.getString(R.string.pref_auto_refresh_interval_key), 60);
    }

    public boolean isYoutubeMobileLinks() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_youtube_mobile_links_key), false);
    }

    public boolean isDisplayNames() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_display_name_key), false);
    }

    public boolean isDisplayAllBoards() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_display_all_boards_key), false);
    }

    public int getTheme() {
        final String defaultTextSizeValue = this.mResources.getString(R.string.pref_text_size_default_value);
        final String defaultThemeValue = this.mResources.getString(R.string.pref_theme_default_value);

        String theme = this.mSettings.getString(this.mResources.getString(R.string.pref_theme_key), defaultThemeValue);
        String textSize = this.mSettings.getString(this.mResources.getString(R.string.pref_text_size_key), defaultTextSizeValue);

        if (theme.equals(defaultThemeValue)) {
            if (textSize.equals(defaultTextSizeValue)) {
                return R.style.Theme_Light_Medium;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_large_value))) {
                return R.style.Theme_Light_Large;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_larger_value))) {
                return R.style.Theme_Light_Larger;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_huge_value))) {
                return R.style.Theme_Light_Huge;
            }

            return R.style.Theme_Light_Medium;
        } else if (theme.equals(this.mResources.getString(R.string.pref_theme_dark_value))) {
            if (textSize.equals(defaultTextSizeValue)) {
                return R.style.Theme_Dark_Medium;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_large_value))) {
                return R.style.Theme_Dark_Large;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_larger_value))) {
                return R.style.Theme_Dark_Larger;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_huge_value))) {
                return R.style.Theme_Dark_Huge;
            }

            return R.style.Theme_Dark_Medium;
        } else if (theme.equals(this.mResources.getString(R.string.pref_theme_photon_value))) {
            if (textSize.equals(defaultTextSizeValue)) {
                return R.style.Theme_Photon_Medium;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_large_value))) {
                return R.style.Theme_Photon_Large;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_larger_value))) {
                return R.style.Theme_Photon_Larger;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_huge_value))) {
                return R.style.Theme_Photon_Huge;
            }

            return R.style.Theme_Photon_Medium;
        }

        return R.style.Theme_Light_Medium;
    }

    public SettingsEntity getCurrentSettings() {
        SettingsEntity result = new SettingsEntity();
        result.theme = this.getTheme();
        result.isDisplayDate = this.isDisplayPostItemDate();
        result.isLocalDate = this.isLocalDateTime();
        result.isLoadThumbnails = this.isLoadThumbnails();

        return result;
    }
}
