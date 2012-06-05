package com.vortexwolf.dvach.asynctasks;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.interfaces.ICancellable;
import com.vortexwolf.dvach.interfaces.ICaptchaView;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.INetworkResourceLoader;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

public class DownloadCaptchaTask extends AsyncTask<String, Void, Boolean> implements ICancellable {
	private static final String TAG = "DownloadCaptchaTask";
	
	private final ICaptchaView mView;
	private final IJsonApiReader mJsonReader;
	private final String mBoard;
	private final String mThreadNumberd;
	private final INetworkResourceLoader mNetworkResourceLoader;
	private final IHtmlCaptchaChecker mHtmlCaptchaChecker;
	
	private boolean mCanSkip = false;
	private CaptchaEntity mCaptcha;
	private Bitmap mCaptchaImage;
	private String mUserError;
	
	public DownloadCaptchaTask(ICaptchaView view, String board, String threadNumber, IJsonApiReader jsonReader, INetworkResourceLoader networkResourceLoader, IHtmlCaptchaChecker htmlCaptchaChecker) {
		this.mView = view;
		this.mJsonReader = jsonReader;
		this.mBoard = board;
		this.mThreadNumberd = threadNumber;
		this.mNetworkResourceLoader = networkResourceLoader;
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

			this.mCaptchaImage = this.mNetworkResourceLoader.loadBitmap(Uri.parse(this.mCaptcha.getUrl()));
			
			return true;
		}
		catch(JsonApiReaderException e){
			MyLog.e(TAG, e);
			this.mUserError = e.getMessage();
			return false;
		}
	}
}
