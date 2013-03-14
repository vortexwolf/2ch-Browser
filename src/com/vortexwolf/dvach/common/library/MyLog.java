package com.vortexwolf.dvach.common.library;

import org.apache.http.StatusLine;

import android.util.Log;

import com.vortexwolf.dvach.common.Constants;

public class MyLog {

    public static void d(String tag, String msg) {
        if (Constants.LOGGING) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (Constants.LOGGING) {
            Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (Constants.LOGGING) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, Throwable e) {
        if (Constants.LOGGING) {
            String message = e.getMessage() != null ? e.getMessage().toString() : "";

            if (e.getCause() != null) {
                Log.e(tag, e.getCause().getMessage().toString() + " " + message);
            } else {
                Class<? extends Throwable> c = e.getClass();
                Log.e(tag, c != null ? c.getName() : "Exception" + " " + message);
            }
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