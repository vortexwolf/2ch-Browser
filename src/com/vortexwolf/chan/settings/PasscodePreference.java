package com.vortexwolf.chan.settings;

import com.vortexwolf.chan.asynctasks.CheckPasscodeTask;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class PasscodePreference extends EditTextPreference {

    public PasscodePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        // check the entered passcode
        if (positiveResult) {
            String passcode = this.getText();
            this.sendPasscodeToServer(passcode);
        }
    }
    
    public void sendPasscodeToServer(String passcode) {
        ApplicationSettings settings = Factory.getContainer().resolve(ApplicationSettings.class);

        CheckPasscodeTask task = new CheckPasscodeTask(this.getContext(), settings, passcode);
        task.execute();
    }
}
