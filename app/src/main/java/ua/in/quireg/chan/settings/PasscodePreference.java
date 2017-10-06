package ua.in.quireg.chan.settings;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.asynctasks.CheckPasscodeTask;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.interfaces.ICheckPasscodeView;

public class PasscodePreference extends EditTextPreference {

    public PasscodePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        this.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String passcode = getText();
                sendPasscodeToServer(passcode);
                return true;
            }
        });
        super.onClick();

    }

    public void sendPasscodeToServer(String passcode) {
        CheckPasscodeTask task = new CheckPasscodeTask(Websites.getDefault(), new CheckPasscodeView(), passcode);
        task.execute();
    }

    private class CheckPasscodeView implements ICheckPasscodeView {
        @Override
        public void onPasscodeRemoved() {
            // show nothing
        }

        @Override
        public void onPasscodeChecked(boolean isSuccess, String errorMessage) {
            Context context = PasscodePreference.this.getContext();
            if (isSuccess) {
                AppearanceUtils.showToastMessage(context, context.getString(R.string.notification_passcode_correct));
            } else {
                String error = !StringUtils.isEmpty(errorMessage) ? errorMessage : context.getString(R.string.notification_passcode_incorrect);
                AppearanceUtils.showToastMessage(context, error);
            }
        }
    }
}
