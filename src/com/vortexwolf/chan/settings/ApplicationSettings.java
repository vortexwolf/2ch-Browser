package com.vortexwolf.chan.settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.ObjectSerializer;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.models.domain.BoardModel;
import com.vortexwolf.chan.models.domain.CaptchaType;

public class ApplicationSettings {

    public static final String TAG = "ApplicationSettings";

    private final SharedPreferences mSettings;
    private final Resources mResources;
    private ArrayList<BoardModel> boards = null;

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

    public void saveAdultAccessCookie(Cookie cookie) {
        SharedPreferences.Editor editor = this.mSettings.edit();
        editor.putString(this.mResources.getString(R.string.pref_adult_access_cookie_key), cookie.getValue());
        editor.putString(this.mResources.getString(R.string.pref_adult_access_cookie_domain_key), cookie.getDomain());
        editor.commit();
    }

    public void saveRecentHistoryTab(int position) {
        SharedPreferences.Editor editor = this.mSettings.edit();
        editor.putInt(this.mResources.getString(R.string.pref_history_tab_position), position);
        editor.commit();
    }

    public String getPasscodeCookieValue() {
        return this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_cookie_key), null);
    }

    public String getPasscodeRaw() {
        return this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_key), null);
    }

    public BasicClientCookie getPassCodeCookie() {
        String domain = this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_cookie_domain_key), null);
        String value = this.mSettings.getString(this.mResources.getString(R.string.pref_passcode_cookie_key), null);
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        BasicClientCookie c = new BasicClientCookie(Constants.USERCODE_NOCAPTCHA_COOKIE, value);
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


    public BasicClientCookie getAdultAccessCookie() {
        String domain = this.mSettings.getString(this.mResources.getString(R.string.pref_adult_access_cookie_domain_key), null);
        String value = this.mSettings.getString(this.mResources.getString(R.string.pref_adult_access_cookie_key), null);
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        BasicClientCookie c = new BasicClientCookie(Constants.ADULT_ACCESS_COOKIE, value);
        c.setDomain(!StringUtils.isEmpty(domain) ? domain : "." + Constants.DEFAULT_DOMAIN);
        c.setPath("/");
        return c;
    }

    public int getRecentHistoryTab() {
        int position = this.mSettings.getInt(this.mResources.getString(R.string.pref_history_tab_position), -1);
        return position;
    }

    public File getDownloadDirectory() {
        String path = this.mSettings.getString(this.mResources.getString(R.string.pref_download_path_key), null);

        String dir = !StringUtils.isEmptyOrWhiteSpace(path) ? path : Constants.DEFAULT_DOWNLOAD_FOLDER;
        dir = dir.startsWith("/storage") ? dir : Environment.getExternalStorageDirectory() + dir;
        return new File(dir);
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

    public boolean isVideoMute() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_video_mute_key), false);
    }

    public int getImageView() {
        final String subScaleViewValue = this.mResources.getString(R.string.pref_image_preview_subscaleview_value);
        String method = this.mSettings.getString(this.mResources.getString(R.string.pref_image_preview_key), subScaleViewValue);
        if (method.equals(subScaleViewValue) && Constants.SDK_VERSION >= 10) {
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

    public int getVideoPlayer() {
        final String autoValue = this.mResources.getString(R.string.pref_video_player_auto_value);
        String value = this.mSettings.getString(this.mResources.getString(R.string.pref_video_player_key), autoValue);

        int webViewWorkingVersion = 21;
        int videoViewWorkingVersion = 10;

        if (value.equals(this.mResources.getString(R.string.pref_video_player_external_1click_value))) {
            return Constants.VIDEO_PLAYER_EXTERNAL_1CLICK;
        } else if (value.equals(this.mResources.getString(R.string.pref_video_player_external_2click_value))) {
            return Constants.VIDEO_PLAYER_EXTERNAL_2CLICK;
        } else if (Constants.SDK_VERSION >= webViewWorkingVersion
            && value.equals(this.mResources.getString(R.string.pref_video_player_webview_value))) {
            return Constants.VIDEO_PLAYER_WEBVIEW;
        } else if (Constants.SDK_VERSION >= videoViewWorkingVersion
            && value.equals(this.mResources.getString(R.string.pref_video_player_videoview_value))) {
            return Constants.VIDEO_PLAYER_VIDEOVIEW;
        }

        if (this.mSettings.getBoolean(this.mResources.getString(R.string.pref_external_video_key), false)) {
            // Check legacy setting 'External video player'. It can be removed in the future.
            return Constants.VIDEO_PLAYER_EXTERNAL_1CLICK;
        } else if (Constants.SDK_VERSION >= videoViewWorkingVersion) {
            return Constants.VIDEO_PLAYER_VIDEOVIEW;
        }

        return Constants.VIDEO_PLAYER_EXTERNAL_1CLICK;
    }

    public boolean isMobileApi() {
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_mobileapi_key), true);
    }

    public int getTheme() {
        final String defaultTextSizeValue = this.mResources.getString(R.string.pref_text_size_13_value);
        final String defaultThemeValue = this.mResources.getString(R.string.pref_theme_white_value);

        String theme = this.mSettings.getString(this.mResources.getString(R.string.pref_theme_key), defaultThemeValue);
        String textSize = this.mSettings.getString(this.mResources.getString(R.string.pref_text_size_key), defaultTextSizeValue);

        if (theme.equals(this.mResources.getString(R.string.pref_theme_white_value))) {
            if (textSize.equals(this.mResources.getString(R.string.pref_text_size_13_value))) {
                return R.style.Theme_Light_13;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_14_value))) {
                return R.style.Theme_Light_14;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_15_value))) {
                return R.style.Theme_Light_15;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_16_value))) {
                return R.style.Theme_Light_16;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_18_value))) {
                return R.style.Theme_Light_18;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_20_value))) {
                return R.style.Theme_Light_20;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_24_value))) {
                return R.style.Theme_Light_24;
            }
        } else if (theme.equals(this.mResources.getString(R.string.pref_theme_black_value))) {
            if (textSize.equals(this.mResources.getString(R.string.pref_text_size_13_value))) {
                return R.style.Theme_Black_13;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_14_value))) {
                return R.style.Theme_Black_14;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_15_value))) {
                return R.style.Theme_Black_15;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_16_value))) {
                return R.style.Theme_Black_16;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_18_value))) {
                return R.style.Theme_Black_18;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_20_value))) {
                return R.style.Theme_Black_20;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_24_value))) {
                return R.style.Theme_Black_24;
            }
        } else if (theme.equals(this.mResources.getString(R.string.pref_theme_neutron_value))) {
            if (textSize.equals(this.mResources.getString(R.string.pref_text_size_13_value))) {
                return R.style.Theme_Neutron_13;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_14_value))) {
                return R.style.Theme_Neutron_14;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_15_value))) {
                return R.style.Theme_Neutron_15;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_16_value))) {
                return R.style.Theme_Neutron_16;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_18_value))) {
                return R.style.Theme_Neutron_18;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_20_value))) {
                return R.style.Theme_Neutron_20;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_24_value))) {
                return R.style.Theme_Neutron_24;
            }
        } else if (theme.equals(this.mResources.getString(R.string.pref_theme_photon_value))) {
            if (textSize.equals(this.mResources.getString(R.string.pref_text_size_13_value))) {
                return R.style.Theme_Photon_13;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_14_value))) {
                return R.style.Theme_Photon_14;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_15_value))) {
                return R.style.Theme_Photon_15;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_16_value))) {
                return R.style.Theme_Photon_16;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_18_value))) {
                return R.style.Theme_Photon_18;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_20_value))) {
                return R.style.Theme_Photon_20;
            } else if (textSize.equals(this.mResources.getString(R.string.pref_text_size_24_value))) {
                return R.style.Theme_Photon_24;
            }
        }

        return R.style.Theme_Light_13;
    }

    public CaptchaType getCaptchaType() {
        // sometimes users can choose more than 1 captcha type, so I will leave it in settings
        return CaptchaType.DVACH;
    }
    @SuppressWarnings("unchecked")
    public ArrayList<BoardModel> getBoards(){
        try {
            //it might be already deserialized
            if(boards != null){
                return boards;
            }
            boards = (ArrayList<BoardModel>) ObjectSerializer.deserialize(mSettings.getString("boards", null));
            if(boards != null) return boards;
        } catch (IOException e){
            e.printStackTrace();
        }
        return new ArrayList<BoardModel>();
    }

    public boolean setBoards(ArrayList<BoardModel> boards) {
        SharedPreferences.Editor editor = mSettings.edit();
        try {
            editor.putString("boards", ObjectSerializer.serialize(boards));
            return editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSwipeToRefresh(){
        return this.mSettings.getBoolean(this.mResources.getString(R.string.pref_swipe_to_refresh_key), true);
    }

    public SettingsEntity getCurrentSettings() {
        SettingsEntity result = new SettingsEntity();
        result.theme = this.getTheme();
        result.isDisplayDate = this.isDisplayPostItemDate();
        result.isLocalDate = this.isLocalDateTime();
        result.isLoadThumbnails = this.isLoadThumbnails();
        result.isDisplayAllBoards = this.isDisplayAllBoards();
        result.isSwipeToRefresh = this.isSwipeToRefresh();

        return result;
    }


}
