package com.vortexwolf.chan.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;

import com.vortexwolf.chan.common.Constants;

public class UriUtils {

    public static Uri getUriForDomain(String domain, boolean isHttps) {
        String scheme = isHttps ? "https" : "http";
        String url = domain.replaceAll("^(?:https?\\://)?(.*?)/*$", scheme + "://$1/");

        return Uri.parse(url);
    }

    public static boolean isImageUri(Uri uri) {        
        if (uri == null) {
            return false;
        }
        
        return RegexUtils.isImagePathString(uri.toString());
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
}
