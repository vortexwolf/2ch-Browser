package ua.in.quireg.chan.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.StyleRes;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.domain.CaptchaType;
import ua.in.quireg.chan.services.SerializationService;

public class ApplicationSettings {

    private Resources mResources;
    private SharedPreferences mSharedPrefs;

    public ApplicationSettings(Context context, SharedPreferences sharedPreferences)  {
        mResources = context.getResources();
        mSharedPrefs = sharedPreferences;
    }

    public void savePassCodeCookie(Cookie cookie) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(mResources.getString(R.string.pref_passcode_cookie_key), cookie.getValue());
        editor.putString(mResources.getString(R.string.pref_passcode_cookie_domain_key), cookie.getDomain());
        editor.apply();
    }

    public void clearPassCodeCookie() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.remove(mResources.getString(R.string.pref_passcode_cookie_key));
        editor.remove(mResources.getString(R.string.pref_passcode_cookie_domain_key));
        editor.apply();
    }

    public void saveCloudflareClearanceCookie(Cookie cookie) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(mResources.getString(R.string.pref_cf_clearance_cookie_key), cookie.getValue());
        editor.putString(mResources.getString(R.string.pref_cf_clearance_cookie_domain_key), cookie.getDomain());
        editor.apply();
    }

    public void saveAdultAccessCookie(Cookie cookie) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(mResources.getString(R.string.pref_adult_access_cookie_key), cookie.getValue());
        editor.putString(mResources.getString(R.string.pref_adult_access_cookie_domain_key), cookie.getDomain());
        editor.apply();
    }

    public void saveRecenoryTab(int position) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putInt(mResources.getString(R.string.pref_history_tab_position), position);
        editor.apply();
    }

    public String getPasscodeCookieValue() {
        return mSharedPrefs.getString(mResources.getString(R.string.pref_passcode_cookie_key), null);
    }

    public String getPasscodeRaw() {
        return mSharedPrefs.getString(mResources.getString(R.string.pref_passcode_key), null);
    }

    public BasicClientCookie getPassCodeCookie() {
        String domain = mSharedPrefs.getString(mResources.getString(R.string.pref_passcode_cookie_domain_key), null);
        String value = mSharedPrefs.getString(mResources.getString(R.string.pref_passcode_cookie_key), null);
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        BasicClientCookie c = new BasicClientCookie(Constants.USERCODE_NOCAPTCHA_COOKIE, value);
        c.setDomain(!StringUtils.isEmpty(domain) ? domain : "" + Constants.DEFAULT_DOMAIN);
        c.setPath("/");
        return c;
    }

    public BasicClientCookie getCloudflareClearanceCookie() {
        String domain = mSharedPrefs.getString(mResources.getString(R.string.pref_cf_clearance_cookie_domain_key), null);
        String value = mSharedPrefs.getString(mResources.getString(R.string.pref_cf_clearance_cookie_key), null);
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        BasicClientCookie c = new BasicClientCookie(Constants.CF_CLEARANCE_COOKIE, value);
        c.setDomain(!StringUtils.isEmpty(domain) ? domain : "" + Constants.DEFAULT_DOMAIN);
        c.setPath("/");
        return c;
    }


    public BasicClientCookie getAdultAccessCookie() {
        String domain = mSharedPrefs.getString(mResources.getString(R.string.pref_adult_access_cookie_domain_key), null);
        String value = mSharedPrefs.getString(mResources.getString(R.string.pref_adult_access_cookie_key), null);
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        BasicClientCookie c = new BasicClientCookie(Constants.ADULT_ACCESS_COOKIE, value);
        c.setDomain(!StringUtils.isEmpty(domain) ? domain : "" + Constants.DEFAULT_DOMAIN);
        c.setPath("/");
        return c;
    }

    public int getRecenoryTab() {
        return mSharedPrefs.getInt(mResources.getString(R.string.pref_history_tab_position), -1);
    }

    public File getDownloadDirectory() {
        String path = mSharedPrefs.getString(mResources.getString(R.string.pref_download_path_key), null);

        String dir = !StringUtils.isEmptyOrWhiteSpace(path) ? path : Constants.DEFAULT_DOWNLOAD_FOLDER;
        dir = dir.startsWith("/storage") ? dir : Environment.getExternalStorageDirectory() + dir;
        return new File(dir);
    }

    public String getName() {
        return mSharedPrefs.getString(mResources.getString(R.string.pref_name_key), null);
    }

    public Uri getDomainUri() {

        String domain = mSharedPrefs.getString(mResources.getString(R.string.pref_domain_key), null);
        domain = StringUtils.isEmpty(domain) ? Constants.DEFAULT_DOMAIN : domain;

        return UriUtils.getUriForDomain(domain, isSecuredHttp());
    }

    public boolean isSecuredHttp() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_use_https_key), true);
    }

    public int getLongPostsMaxHeight() {
        String maxHeightStr = mSharedPrefs.getString(mResources.getString(R.string.pref_cut_posts_key), null);
        int defaultValue = 400;

        if (maxHeightStr == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(maxHeightStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getStartPage() {
        String startPage = mSharedPrefs.getString(mResources.getString(R.string.pref_homepage_key), "").toLowerCase();
        return !StringUtils.isEmpty(startPage) ? startPage : null;
    }

    public boolean isLocalDateTime() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_convert_post_date_key), true);
    }

    public boolean isDownloadInBackground() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_download_background_key), true);
    }

    public boolean isLoadThumbnails() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_load_thumbnails_key), true);
    }

    public boolean isDisplayPostItemDate() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_display_post_date_key), false);
    }

    public boolean isLinksInPopup() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_popup_link_key), true);
    }

    public boolean isAutoRefresh() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_auto_refresh_key), false);
    }

    public int getAutoRefreshInterval() {
        return mSharedPrefs.getInt(mResources.getString(R.string.pref_auto_refresh_interval_key), 60);
    }

    public boolean isDisplayNames() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_display_name_key), true);
    }

    public boolean isDisplayAllBoards() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_display_hidden_boards_key), false);
    }

    public boolean isLegacyImageViewer() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_legacy_image_viewer_key), false);
    }

    public boolean isUnsafeSSL() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_unsafe_ssl_key), false);
    }

    public boolean isMultiThumbnailsInThreads() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_multithumbnails_in_threads_key), false);
    }

    public boolean isDisplayZoomControls() {
        return !mSharedPrefs.getBoolean(mResources.getString(R.string.pref_disable_zoom_controls_key), false);
    }

    public boolean isVideoMute() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_video_mute_key), false);
    }

    public int getImageView() {
        final String subScaleViewValue = mResources.getString(R.string.pref_image_preview_subscaleview_value);
        String method = mSharedPrefs.getString(mResources.getString(R.string.pref_image_preview_key), subScaleViewValue);
        if (method.equals(subScaleViewValue) && Constants.SDK_VERSION >= 10) {
            return Constants.IMAGE_VIEW_SUBSCALEVIEW;
        }

        return Constants.IMAGE_VIEW_WEB_VIEW;
    }

    public int getGifView() {
        final String nativeLibValue = mResources.getString(R.string.pref_gif_preview_native_lib_value);
        String method = mSharedPrefs.getString(mResources.getString(R.string.pref_gif_preview_key), nativeLibValue);
        if (method.equals(nativeLibValue)) {
            return Constants.GIF_NATIVE_LIB;
        }

        return Constants.GIF_WEB_VIEW;
    }

    public int getVideoPlayer() {
        final String autoValue = mResources.getString(R.string.pref_video_player_auto_value);
        String value = mSharedPrefs.getString(mResources.getString(R.string.pref_video_player_key), autoValue);

        int webViewWorkingVersion = 21;
        int videoViewWorkingVersion = 10;

        if (value.equals(mResources.getString(R.string.pref_video_player_external_1click_value))) {
            return Constants.VIDEO_PLAYER_EXTERNAL_1CLICK;
        } else if (value.equals(mResources.getString(R.string.pref_video_player_external_2click_value))) {
            return Constants.VIDEO_PLAYER_EXTERNAL_2CLICK;
        } else if (Constants.SDK_VERSION >= webViewWorkingVersion
                && value.equals(mResources.getString(R.string.pref_video_player_webview_value))) {
            return Constants.VIDEO_PLAYER_WEBVIEW;
        } else if (Constants.SDK_VERSION >= videoViewWorkingVersion
                && value.equals(mResources.getString(R.string.pref_video_player_videoview_value))) {
            return Constants.VIDEO_PLAYER_VIDEOVIEW;
        }

        if (mSharedPrefs.getBoolean(mResources.getString(R.string.pref_external_video_key), false)) {
            // Check legacy setting 'External video player'. It can be removed in the future.
            return Constants.VIDEO_PLAYER_EXTERNAL_1CLICK;
        } else if (Constants.SDK_VERSION >= videoViewWorkingVersion) {
            return Constants.VIDEO_PLAYER_VIDEOVIEW;
        }

        return Constants.VIDEO_PLAYER_EXTERNAL_1CLICK;
    }

    public boolean isMobileApi() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_mobileapi_key), true);
    }

    public @StyleRes int getTheme() {

        final String defaultTextSizeValue = mResources.getString(R.string.pref_text_size_13_value);
        final String defaultThemeValue = mResources.getString(R.string.pref_theme_photon_value);

        String theme = mSharedPrefs.getString(mResources.getString(R.string.pref_theme_key), defaultThemeValue);
        String textSize = mSharedPrefs.getString(mResources.getString(R.string.pref_text_size_key), defaultTextSizeValue);

        if (theme.equals(mResources.getString(R.string.pref_theme_white_value))) {

            if (textSize.equals(mResources.getString(R.string.pref_text_size_13_value))) {
                return R.style.Theme_Light_13;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_14_value))) {
                return R.style.Theme_Light_14;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_15_value))) {
                return R.style.Theme_Light_15;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_16_value))) {
                return R.style.Theme_Light_16;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_18_value))) {
                return R.style.Theme_Light_18;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_20_value))) {
                return R.style.Theme_Light_20;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_24_value))) {
                return R.style.Theme_Light_24;
            }
        } else if (theme.equals(mResources.getString(R.string.pref_theme_black_value))) {

            if (textSize.equals(mResources.getString(R.string.pref_text_size_13_value))) {
                return R.style.Theme_Black_13;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_14_value))) {
                return R.style.Theme_Black_14;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_15_value))) {
                return R.style.Theme_Black_15;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_16_value))) {
                return R.style.Theme_Black_16;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_18_value))) {
                return R.style.Theme_Black_18;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_20_value))) {
                return R.style.Theme_Black_20;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_24_value))) {
                return R.style.Theme_Black_24;
            }
        } else if (theme.equals(mResources.getString(R.string.pref_theme_neutron_value))) {

            if (textSize.equals(mResources.getString(R.string.pref_text_size_13_value))) {
                return R.style.Theme_Neutron_13;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_14_value))) {
                return R.style.Theme_Neutron_14;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_15_value))) {
                return R.style.Theme_Neutron_15;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_16_value))) {
                return R.style.Theme_Neutron_16;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_18_value))) {
                return R.style.Theme_Neutron_18;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_20_value))) {
                return R.style.Theme_Neutron_20;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_24_value))) {
                return R.style.Theme_Neutron_24;
            }
        } else if (theme.equals(mResources.getString(R.string.pref_theme_photon_value))) {

            if (textSize.equals(mResources.getString(R.string.pref_text_size_13_value))) {
                return R.style.Theme_Photon_13;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_14_value))) {
                return R.style.Theme_Photon_14;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_15_value))) {
                return R.style.Theme_Photon_15;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_16_value))) {
                return R.style.Theme_Photon_16;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_18_value))) {
                return R.style.Theme_Photon_18;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_20_value))) {
                return R.style.Theme_Photon_20;
            } else if (textSize.equals(mResources.getString(R.string.pref_text_size_24_value))) {
                return R.style.Theme_Photon_24;
            }
        }

        return R.style.Theme_Photon_13;
    }

    public CaptchaType getCaptchaType() {
        // sometimes users can choose more than 1 captcha type, so I will leave it in settings
        return CaptchaType.DVACH;
    }


    public List<String> getAllowedBoardsIds() {
        return Arrays.asList(mResources.getStringArray(R.array.allowed_boards));
    }

    public boolean isSwipeToRefresh() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_swipe_to_refresh_key), true);
    }

    public int getCacheSize() {
        return mSharedPrefs.getInt(mResources.getString(R.string.pref_cache_size_limit_key), 30);
    }

    public int getCacheMediaSize() {
        return Integer.parseInt(mSharedPrefs.getString(mResources.getString(R.string.pref_cache_media_part_limit_key), "60"));
    }

    public int getCachePagesSize() {
        return Integer.parseInt(mSharedPrefs.getString(mResources.getString(R.string.pref_cache_pages_part_limit_key), "20"));
    }

    public int getCacheThumbnailsSize() {
        return Integer.parseInt(mSharedPrefs.getString(mResources.getString(R.string.pref_cache_thumb_part_limit_key), "20"));
    }

    public int getCachePagesThresholdSize() {
        return Integer.parseInt(mSharedPrefs.getString(mResources.getString(R.string.pref_cache_pages_threshold_limit_key), "7"));
    }

    public boolean isDisplayIcons() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_display_icons_key), false);
    }

    public boolean isUseProxy() {
        return mSharedPrefs.getBoolean(mResources.getString(R.string.pref_use_proxy_key), false);
    }

    public ProxySettings getProxySettings() {
        ProxySettings proxySettings = new ProxySettings();
        proxySettings.server = mSharedPrefs.getString(mResources.getString(R.string.pref_proxy_address_key), "");
        proxySettings.port = Integer.parseInt(mSharedPrefs.getString(mResources.getString(R.string.pref_proxy_port_key), "-1"));
        proxySettings.useAuth = mSharedPrefs.getBoolean(mResources.getString(R.string.pref_proxy_auth_key), false);
        proxySettings.login = mSharedPrefs.getString(mResources.getString(R.string.pref_proxy_auth_login_key), "");
        proxySettings.password = mSharedPrefs.getString(mResources.getString(R.string.pref_proxy_auth_pass_key), "");

        return proxySettings;
    }

    public SettingsEntity getCurrentSettings() {
        SettingsEntity result = new SettingsEntity();
        result.theme = getTheme();
        result.isDisplayDate = isDisplayPostItemDate();
        result.isLocalDate = isLocalDateTime();
        result.isLoadThumbnails = isLoadThumbnails();
        result.isDisplayAllBoards = isDisplayAllBoards();
        result.isSwipeToRefresh = isSwipeToRefresh();
        result.isDisplayIcons = isDisplayIcons();

        return result;
    }

    public class ProxySettings {
        String server;
        int port;
        boolean useAuth;
        String login;
        String password;

        public String getServer() {
            return server;
        }

        public int getPort() {
            return port;
        }

        public boolean isUseAuth() {
            return useAuth;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }


}
