package com.vortexwolf.chan.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;

import com.vortexwolf.chan.common.Constants;

public class UriUtils {
    private static final Pattern threadUriPattern = Pattern.compile("/\\w+/res/\\d+\\.html"); // example:
                                                                                              // /b/res/12345.html
    private static final Pattern boardUriPattern = Pattern.compile("/\\w+/?(?:\\d+\\.html)?"); // example:
                                                                                               // /b
                                                                                               // or
                                                                                               // /b/1.html
    private static final Pattern groupsDvachUriPattern = Pattern.compile("^/(\\w+)/?(?:(?:(\\d+).html)|(?:res/(\\d+)\\.html))?$"); // 1:
                                                                                                                                   // board
                                                                                                                                   // name;
                                                                                                                                   // 2:
                                                                                                                                   // page
                                                                                                                                   // number;
                                                                                                                                   // 3:
                                                                                                                                   // thread
                                                                                                                                   // number
    private static final Pattern groupsFileExtensionPattern = Pattern.compile(".*\\.([a-zA-Z0-9]+)$"); // 1:
                                                                                                       // file
                                                                                                       // extension
    private static final Pattern groupsYoutubeCodePattern = Pattern.compile("(?:https?://)?(?:www\\.)?(?:m\\.)?youtube\\.com/(?:(?:v/)|(?:(?:#/)?watch\\?v=))([\\w\\-]{11})"); // 1:
                                                                                                                                                                               // video
                                                                                                                                                                               // code

    public static Uri getUriForDomain(String domain) {
        String url = domain.replaceAll("^(?:http\\://)?(.*?)/*$", "http://$1/");

        return Uri.parse(url);
    }

    public static boolean isThreadUri(Uri uri) {
        return testUriPath(uri, threadUriPattern);
    }

    public static boolean isBoardUri(Uri uri) {
        return testUriPath(uri, boardUriPattern);
    }

    public static boolean isImageUriString(String str) {
        return !StringUtils.isEmpty(str) && UriUtils.isImageUri(Uri.parse(str));
    }
    
    public static boolean isImageUri(Uri uri) {
        String extension = getFileExtension(uri);

        return Constants.IMAGE_EXTENSIONS.contains(extension);
    }

    public static String getBoardName(Uri uri) {
        String boardName = getGroupValue(uri, groupsDvachUriPattern, 1);
        return boardName;
    }

    public static int getBoardPageNumber(Uri uri) {
        String pageNumber = getGroupValue(uri, groupsDvachUriPattern, 2);
        return pageNumber == null ? 0 : Integer.parseInt(pageNumber);
    }

    public static String getThreadNumber(Uri uri) {
        String threadNumber = getGroupValue(uri, groupsDvachUriPattern, 3);
        return threadNumber;
    }

    public static String getFileExtension(Uri uri) {
        String extension = getGroupValue(uri, groupsFileExtensionPattern, 1);
        return extension;
    }

    public static String getYouTubeCode(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        String videoCode = getGroupValue(text, groupsYoutubeCodePattern, 1);
        return videoCode;
    }

    public static boolean isYoutubeUri(Uri uri) {
        if (uri == null) {
            return false;
        }
        String host = uri.getHost();
        return host != null && host.endsWith("youtube.com");
    }

    public static String formatYoutubeUriFromCode(String code) {
        return "http://www.youtube.com/watch?v=" + code;
    }

    private static boolean testUriPath(Uri uri, Pattern pattern) {
        if (uri == null) {
            return false;
        }
        String path = uri.getPath();

        Matcher m = pattern.matcher(path);
        boolean matches = m.matches();

        return matches;
    }

    private static String getGroupValue(Uri uri, Pattern pattern, int groupIndex) {
        if (uri == null) {
            return null;
        }
        String path = uri.getPath();

        return getGroupValue(path, pattern, groupIndex);
    }

    private static String getGroupValue(String str, Pattern pattern, int groupIndex) {
        if (str == null) {
            return null;
        }

        Matcher m = pattern.matcher(str);
        if (m.find() && m.groupCount() > 0) {
            return m.group(groupIndex);
        }

        return null;
    }
}
