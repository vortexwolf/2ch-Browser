package com.vortexwolf.chan.common.library;

import java.io.File;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.interfaces.IDownloadFileView;

public class BackgroundDownloadFileView implements IDownloadFileView {
    private final Context mContext;
    private final boolean mShowNotifications;

    public BackgroundDownloadFileView(Context context) {
        this(context, true);
    }

    public BackgroundDownloadFileView(Context context, boolean showNotifications) {
        this.mContext = context;
        this.mShowNotifications = showNotifications;
    }

    @Override
    public void setCurrentProgress(int value) {
    }

    @Override
    public void setMaxProgress(int value) {
    }

    @Override
    public void showLoading(String message) {
        if (this.mShowNotifications) {
            AppearanceUtils.showToastMessage(this.mContext, message);
        }
    }

    @Override
    public void hideLoading() {
    }

    @Override
    public void setOnCancelListener(OnCancelListener listener) {
    }

    @Override
    public void showSuccess(File file) {
    }

    @Override
    public void showError(String error) {
        if (this.mShowNotifications) {
            AppearanceUtils.showToastMessage(this.mContext, error);
        }
    }

    @Override
    public void showFileExists(File file) {
        if (this.mShowNotifications) {
            this.showError(this.mContext.getString(R.string.error_file_exist));
        }
    }

}
