package com.vortexwolf.dvach.common.library;

import org.apache.http.StatusLine;

import android.util.Log;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.StringUtils;

public class MyLog {

    public static void d(String tag, String msg) {
        if (Constants.LOGGING) {
            Log.d(tag, StringUtils.emptyIfNull(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (Constants.LOGGING) {
            Log.i(tag, StringUtils.emptyIfNull(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (Constants.LOGGING) {
            Log.e(tag, StringUtils.emptyIfNull(msg));
        }
    }

    public static void e(String tag, Throwable e) {
        if (Constants.LOGGING) {
            Log.e(tag, e.toString());
        }
    }

    public static void w(String tag, String msg) {
        if (Constants.LOGGING) {
            Log.w(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (Constants.LOGGING) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, StatusLine status) {
        if (Constants.LOGGING) {
            Log.v(tag, status.getStatusCode() + " " + status.getReasonPhrase());
        }
    }
}