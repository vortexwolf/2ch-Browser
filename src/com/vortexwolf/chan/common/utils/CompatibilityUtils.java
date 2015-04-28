package com.vortexwolf.chan.common.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.TextView;

import com.vortexwolf.chan.common.Constants;

public class CompatibilityUtils {
    public static void setDisplayHomeAsUpEnabled(Activity activity) {
        if (Constants.SDK_VERSION < 11) {
            return;
        }

        CompatibilityUtilsImpl.setDisplayHomeAsUpEnabled(activity);
    }

    public static boolean hasHardwareMenu(Context context) {
        if (Constants.SDK_VERSION < 11) {
            return true;
        }

        return CompatibilityUtilsImpl.hasHardwareMenu(context, Constants.SDK_VERSION);
    }

    public static boolean isTextSelectable(TextView textView) {
        if (Constants.SDK_VERSION < 11) {
            return false;
        }

        return CompatibilityUtilsImpl.isTextSelectable(textView);
    }

    public static void copyText(Activity activity, String label, String text) {
        CompatibilityUtilsImpl.copyText(activity, label, text, Constants.SDK_VERSION);
    }

    public static boolean isTablet(Context context) {
        if (Constants.SDK_VERSION < 4) {
            return false;
        }

        return CompatibilityUtilsImpl.API4.isTablet(context);
    }
}
