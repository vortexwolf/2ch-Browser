package com.vortexwolf.dvach.common.library;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.ICancellable;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.interfaces.IDownloadFileView;

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
	public void setProgress(int value) {
		this.mProgressDialog.setProgress(value);
	}

	@Override
	public void setMax(int value) {
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
