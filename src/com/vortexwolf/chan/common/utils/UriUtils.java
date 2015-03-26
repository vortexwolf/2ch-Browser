package com.vortexwolf.chan.common.utils;

import android.net.Uri;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.interfaces.IUrlBuilder;

public class UriUtils {

    public static Uri getUriForDomain(String domain, boolean isHttps) {
        String scheme = isHttps ? "https" : "http";
        String url = domain.replaceAll("^(?:https?\\://)?(.*?)/*$", scheme + "://$1/");

        return Uri.parse(url);
    }

    public static Uri changeHttpsToHttp(Uri uri) {
        return uri.buildUpon().scheme("http").build();
    }

    public static boolean isImageUri(Uri uri) {
        if (uri == null) {
            return false;
        }

        return RegexUtils.isImagePathString(uri.toString());
    }

    public static boolean isWebmUri(Uri uri) {
        if (uri == null) {
            return false;
        }

        return RegexUtils.getFileExtension(uri.toString()).equalsIgnoreCase("webm");
    }

    public static boolean isYoutubeUri(Uri uri) {
        if (uri == null) {
            return false;
        }

        String host = uri.getHost();
        return host != null && host.endsWith("youtube.com");
    }

    public static String formatYoutubeUri(String code) {
        return "http://www.youtube.com/watch?v=" + code;
    }

    public static String formatYoutubeMobileUri(String code) {
        return "http://m.youtube.com/#/watch?v=" + code;
    }

    public static String formatYoutubeThumbnailUri(String code) {
        return "http://img.youtube.com/vi/" + code + "/default.jpg";
    }

    public static boolean areCookieDomainsEqual(String cookieDomain, String siteDomain) {
        if (!cookieDomain.startsWith(".")) {
            cookieDomain = "." + cookieDomain;
        }
        if (!siteDomain.startsWith(".")) {
            siteDomain = "." + siteDomain;
        }

        return cookieDomain.equalsIgnoreCase(siteDomain);
    }

    public static Uri createRefererUri(IUrlBuilder builder, String boardName, String threadNumber) {
        if(StringUtils.isEmpty(threadNumber) || threadNumber == Constants.ADD_THREAD_PARENT) {
            return Uri.parse(builder.getPageUrlHtml(boardName, 0));
        }

        return Uri.parse(builder.getThreadUrlHtml(boardName, threadNumber));
    }
}
