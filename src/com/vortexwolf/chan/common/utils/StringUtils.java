package com.vortexwolf.chan.common.utils;

public class StringUtils {

    public static String cutIfLonger(String str, int maxLength) {
        if (str.length() > maxLength) {
            return str.substring(0, maxLength) + "...";
        }
        return str;
    }

    public static boolean isEmpty(CharSequence s) {
        return s == null || "".equals(s);
    }

    public static boolean isEmptyOrWhiteSpace(String s) {
        return isEmpty(emptyIfNull(s).trim());
    }

    public static String emptyIfNull(CharSequence s) {
        return s == null ? "" : s.toString();
    }

    public static String nullIfEmpty(String s) {
        return isEmpty(s) ? null : s;
    }
}
