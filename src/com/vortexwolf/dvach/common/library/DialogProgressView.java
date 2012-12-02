package com.vortexwolf.dvach.common.library;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.ICancellable;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.interfaces.IProgressView;

public class DialogProgressView implements IProgressView {
	private final Context mContext;
	private final ProgressDialog mProgressDialog;
	
	public DialogProgressView(Context context) {
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
				DialogProgressView.this.hide();
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
	public void show() {
		int maxValue = this.mProgressDialog.getMax();
		this.mProgressDialog.setMax(Math.max(maxValue, 0));
		
		this.mProgressDialog.show();
	}
	
	@Override
	public void hide() {
		this.mProgressDialog.dismiss();
	}
}
