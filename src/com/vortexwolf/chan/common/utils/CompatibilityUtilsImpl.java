package com.vortexwolf.chan.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.ViewConfiguration;
import android.widget.TextView;

/**
 * I use 2 classes because I still support the version 1.5 which throws
 * VerifyException if to put all code in the single class
 */
@SuppressLint("NewApi")
public class CompatibilityUtilsImpl {

    public static void setDisplayHomeAsUpEnabled(Activity activity) {
        activity.getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static boolean hasHardwareMenu(Context context, int currentVersion) {
        if (currentVersion < 11) {
            return true;
        } else if (currentVersion >= 11 && currentVersion <= 13) {
            return false;
        }

        return ViewConfiguration.get(context).hasPermanentMenuKey();
    }
    
    public static boolean isTextSelectable(TextView textView) {
        return textView.isTextSelectable();
    }
    
    public static void copyText(Activity activity, String label, String text, int currentVersion) {
        if(currentVersion < 11) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE); 
            android.content.ClipData clip = android.content.ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        }
    }
}
