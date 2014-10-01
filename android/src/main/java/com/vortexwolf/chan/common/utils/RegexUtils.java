package com.vortexwolf.chan.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vortexwolf.chan.common.Constants;

import android.net.Uri;

public class RegexUtils {

    private static final Pattern groupsFileExtensionPattern = Pattern.compile(".*\\.([a-zA-Z0-9]+)$");
    private static final Pattern groupsYoutubeCodePattern = Pattern.compile("(?:https?://)?(?:www\\.)?(?:m\\.)?youtube\\.com/(?:(?:v/)|(?:(?:#/)?watch\\?v=))([\\w\\-]{11})"); // 1:

    public static boolean isImagePathString(String str) {
        String extension = getFileExtension(str);
        return Constants.IMAGE_EXTENSIONS.contains(extension);
    }
    
    public static String getYouTubeCode(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }

        return getGroupValue(str, groupsYoutubeCodePattern, 1);
    }
    
    public static String getFileExtension(String str) {
        if (str == null) {
            return null;
        }
        
        return getGroupValue(str, groupsFileExtensionPattern, 1);
    }
    
    public static String[] getGroupValues(Uri uri, Pattern pattern) {
        if (uri == null) {
            return null;
        }
        
        return getGroupValues(uri.getPath(), pattern);
    }
    
    public static String[] getGroupValues(String str, Pattern pattern) {
        if (str == null) {
            return null;
        }

        Matcher m = pattern.matcher(str);
        if (m.find() && m.groupCount() > 0) {
            String[] results = new String[m.groupCount() + 1];
            for (int i = 0; i < m.groupCount() + 1; i++) {
                results[i] = m.group(i);
            }
            return results;
        }

        return null;
    }

    public static String getGroupValue(Uri uri, Pattern pattern, int groupIndex) {
        if (uri == null) {
            return null;
        }
        
        return getGroupValue(uri.getPath(), pattern, groupIndex);
    }
    
    public static String getGroupValue(String str, Pattern pattern, int groupIndex) {
        String[] result = getGroupValues(str, pattern);
        if (result != null && groupIndex < result.length) {
            return result[groupIndex];
        }
        
        return null;
    }
}
