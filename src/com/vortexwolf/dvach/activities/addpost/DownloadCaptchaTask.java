package com.vortexwolf.dvach.activities.addpost;

import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.api.JsonApiReaderException;
import com.vortexwolf.dvach.api.entities.CaptchaEntity;
import com.vortexwolf.dvach.common.library.BitmapManager;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.interfaces.ICancellable;
import com.vortexwolf.dvach.interfaces.ICaptchaView;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpBitmapReader;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class DownloadCaptchaTask extends AsyncTask<String, Void, Boolean> implements ICancellable {
	private static final String TAG = "DownloadCaptchaTask";
	
	private final ICaptchaView mView;
	private final IJsonApiReader mJsonReader;
	private final String mBoard;
	private final String mThreadNumberd;
	private final IHttpBitmapReader mHttpBitmapReader;
	private final IHtmlCaptchaChecker mHtmlCaptchaChecker;
	
	private boolean mCanSkip = false;
	private CaptchaEntity mCaptcha;
	private Bitmap mCaptchaImage;
	private String mUserError;
	
	public DownloadCaptchaTask(ICaptchaView view, String board, String threadNumber, IJsonApiReader jsonReader, IHttpBitmapReader httpBitmapReader, IHtmlCaptchaChecker htmlCaptchaChecker) {
		this.mView = view;
		this.mJsonReader = jsonReader;
		this.mBoard = board;
		this.mThreadNumberd = threadNumber;
		this.mHttpBitmapReader = httpBitmapReader;
		this.mHtmlCaptchaChecker = htmlCaptchaChecker;
	}
	
	@Override
	public void onPreExecute() {
		this.mView.showCaptchaLoading();
	}
	
	@Override
	public void onPostExecute(Boolean success) {
		if(this.mCanSkip){
			this.mView.skipCaptcha();
		}
		else if(success && mCaptcha != null){
			this.mView.showCaptcha(mCaptcha, mCaptchaImage);
		}
		else{
			this.mView.showCaptchaError(this.mUserError);
		}
	}
	
	@Override
	protected Boolean doInBackground(String... params) {

		try{
			this.mCanSkip = this.mHtmlCaptchaChecker.canSkipCaptcha(this.mBoard, this.mThreadNumberd);
			if(this.mCanSkip) return true;
			
			this.mCaptcha = this.mJsonReader.readCaptcha(this.mBoard, this);
			if(this.isCancelled()) return false;

			this.mCaptchaImage = this.mHttpBitmapReader.fromUri(this.mCaptcha.getUrl());
			
			return true;
		}
		catch(JsonApiReaderException e){
			MyLog.e(TAG, e);
			this.mUserError = e.getMessage();
			return false;
		}
	}
}
