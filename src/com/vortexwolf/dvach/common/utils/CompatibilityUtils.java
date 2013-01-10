package com.vortexwolf.dvach.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.vortexwolf.dvach.common.library.MyLog;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.ViewConfiguration;

public class CompatibilityUtils {
    public static final Integer sCurrentVersion = Integer.valueOf(Build.VERSION.SDK);

    public static void setDisplayHomeAsUpEnabled(Activity activity) {
        if(sCurrentVersion < 11) {
            return;
        }
        
        CompatibilityUtilsImpl.setDisplayHomeAsUpEnabled(activity);
    }
    
    public static boolean hasHardwareMenu(Context context){
        if (sCurrentVersion < 11) {
            return true;
        }
        
        return CompatibilityUtilsImpl.hasHardwareMenu(context, sCurrentVersion);
    }
}
