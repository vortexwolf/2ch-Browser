package com.vortexwolf.dvach.presentation.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Errors;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;
import com.vortexwolf.dvach.interfaces.ICancellable;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class DownloadFileTask extends AsyncTask<String, Long, Boolean> implements ICancellable, IProgressChangeListener {
	public static final String TAG = "DownloadFileTask";
	private final IDownloadFileService mDownloadFileService;
	private final String mImageUri;
	private final Context mContext;
	private final Errors mErrors;
	
	private String mUserError = null;
	private File mFileWriteTo;
	private String mSavedFilePath;
	
	private ProgressDialog mProgressDialog;
	private long mContentLength;

	public DownloadFileTask(Context context, String imageUri, File to, Errors errors) {
		super();
		this.mContext = context;
		this.mErrors = errors;
		this.mDownloadFileService = new DownloadFileService(this.mErrors);
		this.mImageUri = imageUri;
		this.mFileWriteTo = to;
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
		try{
			this.mSavedFilePath = this.downloadFile(this.mImageUri, this.mFileWriteTo);
			return true;
		}
		catch (DownloadFileException e){
			this.mUserError = e.getMessage();
			return false;
		}
	}
	
	private String downloadFile(String uri, File fileToWrite) throws DownloadFileException {
		
	    File file = fileToWrite;
	    if(file.exists()){
	    	throw new DownloadFileException(this.mErrors.getFileExistError());
	    }
	    //file.createNewFile();
	    
		try{
    	 // download the file
			MyLog.v(TAG, "start to download " + fileToWrite.getName());
    	    URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            connection.connect();
            this.setContentLength(connection.getContentLength());
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
            	if(this.isCancelled()){
            		input.close();
            		output.close();
            		file.delete();
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
            
            return file.getAbsolutePath();
    	} catch (Exception e) {
    	    MyLog.e(TAG, e);
    	    throw new DownloadFileException(this.mErrors.getSaveFileError());
    	}
	}
	
	@Override
	public void onPreExecute() {
		//Не показывать диалог совсем, если файл существует
		this.mFileWriteTo = this.mDownloadFileService.getSaveFilePath(this.mImageUri);
		if(this.mFileWriteTo.exists()){
			this.cancel(false);
			this.mUserError = this.mErrors.getFileExistError();
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
