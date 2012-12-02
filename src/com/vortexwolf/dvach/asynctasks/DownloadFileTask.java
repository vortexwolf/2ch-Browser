package com.vortexwolf.dvach.asynctasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.library.DialogProgressView;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.exceptions.DownloadFileException;
import com.vortexwolf.dvach.interfaces.ICancellable;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.interfaces.IProgressView;
import com.vortexwolf.dvach.services.domain.DownloadFileService;
import com.vortexwolf.dvach.services.domain.SaveFileService;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Window;

public class DownloadFileTask extends AsyncTask<String, Long, Boolean> implements ICancelled, IProgressChangeListener {
	public static final String TAG = "DownloadFileTask";
	private final DownloadFileService mDownloadFileService;
	private final Context mContext;
	private final Resources mResources;
	private final Uri mFrom;
	private final File mFileWriteTo;
	private final IProgressView mProgressView;
	
	private String mUserError = null;

	public DownloadFileTask(Context context, Uri from, File to, DownloadFileService downloadFileService, IProgressView progressView) {
		super();
		this.mContext = context;
		this.mResources = context.getResources();
		this.mDownloadFileService = downloadFileService;
		this.mFrom = from;
		this.mFileWriteTo = to;
		
		this.mProgressView = progressView;
		if(this.mProgressView != null) {
			this.mProgressView.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
		}
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
		try{
			this.mDownloadFileService.downloadFile(this.mFrom, this.mFileWriteTo, this, this);
			return true;
		}
		catch (DownloadFileException e){
			this.mUserError = e.getMessage();
			return false;
		}
	}
	
	@Override
	public void onPreExecute() {
		//Не показывать диалог совсем, если файл существует
		if(this.mFileWriteTo.exists()){
			this.cancel(false);
			AppearanceUtils.showToastMessage(this.mContext, mResources.getString(R.string.error_file_exist));	
			return;
		}
		
		AppearanceUtils.showToastMessage(this.mContext, this.mContext.getString(R.string.notification_save_image_started, this.mFileWriteTo.getAbsolutePath()));
		
		if(this.mProgressView != null) {
			this.mProgressView.show();
		}
	}
	
	@Override
	public void onPostExecute(Boolean success) {
		if(this.mProgressView != null) {
			this.mProgressView.hide();
		}
		
		if(success){
			// ok
		}
		else{
			AppearanceUtils.showToastMessage(this.mContext, this.mUserError);	
		}
	}
	
	@Override
	public void progressChanged(long newValue) {
		if(this.mProgressView != null) {
			this.mProgressView.setProgress((int)newValue / 1024);
		}
	}
	
	@Override
	public void indeterminateProgress() {
		// nothing
	}

	@Override
	public void setContentLength(long value) {
		if(this.mProgressView != null) {
			this.mProgressView.setMax((int)value / 1024);
		}
	}
}
