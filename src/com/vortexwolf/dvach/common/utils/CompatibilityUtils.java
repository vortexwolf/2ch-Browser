package com.vortexwolf.dvach.common.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.TextView;

public class CompatibilityUtils {
    public static final Integer sCurrentVersion = Integer.valueOf(Build.VERSION.SDK);
    
    public static void setDisplayHomeAsUpEnabled(Activity activity) {
        if (sCurrentVersion < 11) {
            return;
        }

        CompatibilityUtilsImpl.setDisplayHomeAsUpEnabled(activity);
    }

    public static boolean hasHardwareMenu(Context context) {
        if (sCurrentVersion < 11) {
            return true;
        }

        return CompatibilityUtilsImpl.hasHardwareMenu(context, sCurrentVersion);
    }
    
    public static boolean isTextSelectable(TextView textView) {
        if (sCurrentVersion < 11) {
            return false;
        }
        
        return CompatibilityUtilsImpl.isTextSelectable(textView);
    }
}
