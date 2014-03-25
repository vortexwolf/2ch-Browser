package com.vortexwolf.chan.settings;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.asynctasks.CheckPasscodeTask;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;

public class ApplicationSettings {

    public static final String TAG = "ApplicationSettings";

    private final Context mContext;
    private final SharedPreferences mSettings;
    private final Resources mResources;

    public ApplicationSettings(Context context, Resources resources) {
        this.mContext = context;
        this.mSettings = PreferenceManager.getDefaultSharedPreferences(context);
        this.mResources = resources;
    }
    
    public void savePassCodeCookie(String passCodeCookie){
        SharedPreferences.Editor editor = this.mSettings.edit();
        editor.putString(this.mResources.getString(R.string.pref_passcode_cookie_key), passCodeCookie);
        editor.commit();
    }
    
    public String getPassCodeCookie() {
        return this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_cookie_key), null);
    }
    
    public String getPassCode(){
        // should be removed after everyone updates their applications
        return this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_key), null);
    }

    public String getDownloadPath() {
        String path = this.mSettings.getString(this.mResources.getString(R.string.pref_download_path_key), null);

        return !StringUtils.isEmptyOrWhiteSpace(path) ? path : Constants.DEFAULT_DOWNLOAD_FOLDER;
    }

    public String getName() {
        return this.mSettings.getString(this.mResources.getString(R.string.pref_name_key), null);
    }

    public Uri getDomainUri() {
        boolean isHttps = this.mSettings.getBoolean(this.mResources.getString(R.string.pref_use_https_key), false);
        String domain = this.mSettings.getString(this.mResources.getString(R.string.pref_domain_key), null);
        domain = StringUtils.isEmpty(domain) ? Constants.DEFAULT_DOMAIN : domain;
        
        Uri uri = UriUtils.getUriForDomain(domain, isHttps);
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
    
    public String getStartPage(){
        String startPage = this.mSettings.getString(this.mResources.getString(R.string.pref_homepage_key), "").toLowerCase();
        return !StringUtils.isEmpty(startPage) ? startPage : null;
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
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_display_hidden_boards_key), false);
    }
    
    public boolean isLegacyImageViewer() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_legacy_image_viewer_key), false);
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
        result.isDisplayAllBoards = this.isDisplayAllBoards();

        return result;
    }
}
