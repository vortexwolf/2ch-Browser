package com.vortexwolf.chan.settings;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;

public class ApplicationSettings {

    public static final String TAG = "ApplicationSettings";

    private final SharedPreferences mSettings;
    private final Resources mResources;

    public ApplicationSettings(Context context, Resources resources) {
        this.mSettings = PreferenceManager.getDefaultSharedPreferences(context);
        this.mResources = resources;
    }

    public void savePassCodeCookie(Cookie cookie) {
        SharedPreferences.Editor editor = this.mSettings.edit();
        editor.putString(this.mResources.getString(R.string.pref_passcode_cookie_key), cookie.getValue());
        editor.putString(this.mResources.getString(R.string.pref_passcode_cookie_domain_key), cookie.getDomain());
        editor.commit();
    }
    
    public void clearPassCodeCookie() {
        SharedPreferences.Editor editor = this.mSettings.edit();
        editor.remove(this.mResources.getString(R.string.pref_passcode_cookie_key));
        editor.remove(this.mResources.getString(R.string.pref_passcode_cookie_domain_key));
        editor.commit();
    }

    public void saveCloudflareClearanceCookie(Cookie cookie) {
        SharedPreferences.Editor editor = this.mSettings.edit();
        editor.putString(this.mResources.getString(R.string.pref_cf_clearance_cookie_key), cookie.getValue());
        editor.putString(this.mResources.getString(R.string.pref_cf_clearance_cookie_domain_key), cookie.getDomain());
        editor.commit();
    }
    
    public void saveRecentHistoryTab(int position) {
        SharedPreferences.Editor editor = this.mSettings.edit();
        editor.putInt(this.mResources.getString(R.string.pref_history_tab_position), position);
        editor.commit();
    }

    public String getPassCodeValue() {
        return this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_cookie_key), null);
    }
    
    public BasicClientCookie getPassCodeCookie() {
        String domain = this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_cookie_domain_key), null);
        String value = this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_cookie_key), null);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        
        BasicClientCookie c = new BasicClientCookie(Constants.USERCODE_COOKIE, value);
        c.setDomain(!StringUtils.isEmpty(domain) ? domain : "." + Constants.DEFAULT_DOMAIN);
        c.setPath("/");
        return c;
    }

    public BasicClientCookie getCloudflareClearanceCookie() {
        String domain = this.mSettings.getString(this.mResources.getString(R.string.pref_cf_clearance_cookie_domain_key), null);
        String value = this.mSettings.getString(this.mResources.getString(R.string.pref_cf_clearance_cookie_key), null);
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        BasicClientCookie c = new BasicClientCookie(Constants.CF_CLEARANCE_COOKIE, value);
        c.setDomain(!StringUtils.isEmpty(domain) ? domain : "." + Constants.DEFAULT_DOMAIN);
        c.setPath("/");
        return c;
    }
    
    public int getRecentHistoryTab() {
        int position = this.mSettings.getInt(this.mResources.getString(R.string.pref_history_tab_position), -1);
        return position;
    }
    
    public String getDownloadPath() {
        String path = this.mSettings.getString(this.mResources.getString(R.string.pref_download_path_key), null);

        return !StringUtils.isEmptyOrWhiteSpace(path) ? path : Constants.DEFAULT_DOWNLOAD_FOLDER;
    }

    public String getName() {
        return this.mSettings.getString(this.mResources.getString(R.string.pref_name_key), null);
    }

    public Uri getDomainUri() {
        boolean isHttps = this.mSettings.getBoolean(this.mResources.getString(R.string.pref_use_https_key), true);
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
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getStartPage() {
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

    public boolean isAutoRefresh() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_auto_refresh_key), false);
    }

    public int getAutoRefreshInterval() {
        return this.mSettings.getInt(this.mResources.getString(R.string.pref_auto_refresh_interval_key), 60);
    }

    public boolean isDisplayNames() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_display_name_key), true);
    }

    public boolean isDisplayAllBoards() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_display_hidden_boards_key), false);
    }

    public boolean isLegacyImageViewer() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_legacy_image_viewer_key), false);
    }

    public boolean isUnlimitedCache() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_file_cache_no_limit_key), false);
    }
    
    public boolean isUnsafeSSL() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_unsafe_ssl_key), false);
    }
    
    public boolean isMultiThumbnailsInThreads() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_multithumbnails_in_threads_key), false);
    }
    
    public boolean isDisplayZoomControls() {
        return !this.mSettings.getBoolean(this.mResources.getString(R.string.pref_disable_zoom_controls_key), false);
    }
    
    public int getImageView() {
        final String subScaleViewValue = this.mResources.getString(R.string.pref_image_preview_subscaleview_value);
        String method = this.mSettings.getString(this.mResources.getString(R.string.pref_image_preview_key), subScaleViewValue);
        if (method.equals(subScaleViewValue)) {
            return Constants.IMAGE_VIEW_SUBSCALEVIEW;
        }
        
        return Constants.IMAGE_VIEW_WEB_VIEW;
    }
    
    public int getGifView() {
        final String nativeLibValue = this.mResources.getString(R.string.pref_gif_preview_native_lib_value);
        String method = this.mSettings.getString(this.mResources.getString(R.string.pref_gif_preview_key), nativeLibValue);
        if (method.equals(nativeLibValue)) {
            return Constants.GIF_NATIVE_LIB;
        }
        
        return Constants.GIF_WEB_VIEW;
    }
    
    public int getVideoPreviewMethod() {
        final String defaultMethodValue = this.mResources.getString(R.string.pref_video_preview_default_value);
        final String changeDomainMethodValue = this.mResources.getString(R.string.pref_video_preview_change_domain_value);
        final String downloadMethodValue = this.mResources.getString(R.string.pref_video_preview_download_value);
        String method = this.mSettings.getString(this.mResources.getString(R.string.pref_video_preview_key), defaultMethodValue);
        if (method.equals(downloadMethodValue)) return Constants.VIDEO_PREVIEW_METHOD_DOWNLOAD;
        if (method.equals(changeDomainMethodValue)) return Constants.VIDEO_PREVIEW_METHOD_CHANGE_DOMAIN;
        return Constants.VIDEO_PREVIEW_METHOD_DEFAULT;
    }
    
    public boolean isMobileApi() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_mobileapi_key), true);
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
