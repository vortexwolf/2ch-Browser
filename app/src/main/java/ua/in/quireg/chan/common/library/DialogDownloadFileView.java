package ua.in.quireg.chan.common.library;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.interfaces.IDownloadFileView;

public class DialogDownloadFileView implements IDownloadFileView {
    private final Context mContext;
    private final ProgressDialog mProgressDialog;

    public DialogDownloadFileView(Context context) {
        this.mContext = context;
        this.mProgressDialog = new ProgressDialog(this.mContext);
        this.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.mProgressDialog.setMessage(this.mContext.getString(R.string.loading));
        this.mProgressDialog.setCancelable(true);
    }

    @Override
    public void setOnCancelListener(final DialogInterface.OnCancelListener listener) {
        this.mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                listener.onCancel(dialog);
                DialogDownloadFileView.this.hideLoading();
            }
        });
    }

    @Override
    public void setCurrentProgress(int value) {
        this.mProgressDialog.setProgress(value);
    }

    @Override
    public void setMaxProgress(int value) {
        this.mProgressDialog.setMax(value);
    }

    @Override
    public void showLoading(String message) {
        int maxValue = this.mProgressDialog.getMax();
        this.mProgressDialog.setMax(Math.max(maxValue, 0));

        this.mProgressDialog.show();

        AppearanceUtils.showToastMessage(this.mContext, message);
    }

    @Override
    public void hideLoading() {
        this.mProgressDialog.dismiss();
    }

    @Override
    public void showSuccess(File file) {
    }

    @Override
    public void showError(String error) {
        AppearanceUtils.showToastMessage(this.mContext, error);
    }

    @Override
    public void showFileExists(File file) {
        this.showError(this.mContext.getString(R.string.error_file_exist));
    }
}
