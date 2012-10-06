package com.vortexwolf.dvach.services.domain;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.exceptions.DownloadFileException;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;
import com.vortexwolf.dvach.interfaces.ICancellable;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;

public class DownloadFileTask extends AsyncTask<String, Long, Boolean> implements ICancellable, IProgressChangeListener {
	public static final String TAG = "DownloadFileTask";
	private final IDownloadFileService mDownloadFileService;
	private final Context mContext;
	private final Resources mResources;
	private final Uri mFrom;
	private final File mFileWriteTo;
	
	private String mUserError = null;
	private String mSavedFilePath;
	
	private ProgressDialog mProgressDialog;
	private long mContentLength;

	public DownloadFileTask(Context context, Uri from, File to) {
		super();
		this.mContext = context;
		this.mResources = context.getResources();
		this.mDownloadFileService = Factory.getContainer().resolve(IDownloadFileService.class);
		this.mFrom = from;
		this.mFileWriteTo = to;
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
		try{
			this.mSavedFilePath = this.downloadFile(this.mFrom, this.mFileWriteTo);
			return true;
		}
		catch (DownloadFileException e){
			this.mUserError = e.getMessage();
			return false;
		}
	}
	
	private String downloadFile(Uri uri, File to) throws DownloadFileException {
	    if(to.exists()){
	    	throw new DownloadFileException(mResources.getString(R.string.error_file_exist));
	    }

		try{
			// download the file
			MyLog.v(TAG, "start to download " + to.getName());
			
		    File fromFile = new File(uri.getPath());
		    BufferedInputStream input;
		    	
		    if(fromFile.exists()){
		    	this.setContentLength(fromFile.length());
		    	input = new BufferedInputStream(new FileInputStream(fromFile));
		    }
		    else {
	    	    URL url = new URL(uri.toString());
	            URLConnection connection = url.openConnection();
	            connection.connect();
	            
	            this.setContentLength(connection.getContentLength());
	            input = new BufferedInputStream(url.openStream());
		    }
		    
            byte data[] = new byte[1024];
            long total = 0;
            int count;
            OutputStream output = new FileOutputStream(to);
            
            while ((count = input.read(data)) != -1) {
            	if(this.isCancelled()){
            		input.close();
            		output.close();
            		to.delete();
            		MyLog.v(TAG, "download file cancelled");
            		return null;
            	}
            	
                total += count;
                this.progressChanged(total - count, total);
                output.write(data, 0, count);
            }
            
            MyLog.v(TAG, "file downloaded");
            output.close();
            input.close();
            
            return to.getAbsolutePath();
    	} catch (Exception e) {
    	    MyLog.e(TAG, e);
    	    throw new DownloadFileException(mResources.getString(R.string.error_save_file));
    	}
	}
	
	@Override
	public void onPreExecute() {
		//Не показывать диалог совсем, если файл существует
		if(this.mFileWriteTo.exists()){
			this.cancel(false);
			this.mUserError = mResources.getString(R.string.error_file_exist);
			this.showError();
			return;
		}
		
		mProgressDialog = new ProgressDialog(this.mContext);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMessage(this.mContext.getString(R.string.loading));
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
					cancel(true);
					mProgressDialog.dismiss();
			}
		});
		
		mProgressDialog.show();
		mProgressDialog.setMax(0);
	}
	
	@Override
	public void onPostExecute(Boolean success) {
		this.mProgressDialog.dismiss();
		
		if(success){
			MyLog.v("DownloadFileTask", this.mSavedFilePath);
			AppearanceUtils.showToastMessage(this.mContext, this.mContext.getString(R.string.notification_save_image_success, this.mSavedFilePath));
		}
		else{
			this.showError();
		}
	}
	
	private void showError(){
		AppearanceUtils.showToastMessage(this.mContext, this.mUserError);	
	}

	@Override
	protected void onProgressUpdate(Long... args) {
		int progressValue = args[0].intValue();
		mProgressDialog.setProgress(progressValue);
	}

	@Override
	public void progressChanged(long oldValue, long newValue) {
		this.publishProgress(newValue / 1024);
	}

	@Override
	public void setContentLength(long value) {
		this.mProgressDialog.setMax((int)value / 1024);
	}
}
