package com.vortexwolf.chan.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.asynctasks.CheckPasscodeTask;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.interfaces.ICheckPasscodeView;

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
        CheckPasscodeTask task = new CheckPasscodeTask(new CheckPasscodeView(), passcode);
        task.execute();
    }

    private class CheckPasscodeView implements ICheckPasscodeView {
        @Override
        public void onPasscodeRemoved() {
            // show nothing
        }

        @Override
        public void onPasscodeChecked(boolean isSuccess) {
            Context context = PasscodePreference.this.getContext();
            if (isSuccess) {
                AppearanceUtils.showToastMessage(context, context.getString(R.string.notification_passcode_correct));
            } else {
                AppearanceUtils.showToastMessage(context, context.getString(R.string.notification_passcode_incorrect));
            }
        }
    }
}
