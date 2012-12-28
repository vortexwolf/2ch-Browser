package com.vortexwolf.dvach.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.vortexwolf.dvach.common.library.MyLog;

import android.app.Activity;

public class CompatibilityUtils {
    public static final String TAG = "CompatibilityUtils";

    private static Method sActivity_GetActionBar;

    static {
        try {
            sActivity_GetActionBar = Activity.class.getMethod("getActionBar");
        } catch (NoSuchMethodException nsme) {
        }
    };

    public static void setDisplayHomeAsUpEnabled(Activity activity) {
        if (sActivity_GetActionBar != null) {
            try {
                Object actionBar = sActivity_GetActionBar.invoke(activity);
                Method homeAsUp = actionBar.getClass().getMethod("setDisplayHomeAsUpEnabled", new Class[] { Boolean.TYPE });
                homeAsUp.invoke(actionBar, true);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
    }
}
