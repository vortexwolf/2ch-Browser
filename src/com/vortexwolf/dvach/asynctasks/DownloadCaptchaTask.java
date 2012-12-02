package com.vortexwolf.dvach.asynctasks;

import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.ICaptchaView;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.INetworkResourceLoader;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;
import com.vortexwolf.dvach.services.domain.HttpStringReader;
import com.vortexwolf.dvach.services.domain.RecaptchaService;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

public class DownloadCaptchaTask extends AsyncTask<String, Void, Boolean> implements ICancelled {
	private static final String TAG = "DownloadCaptchaTask";
	
	private final ICaptchaView mView;
	private final IJsonApiReader mJsonReader;
	private final String mBoard;
	private final String mThreadNumberd;
	private final INetworkResourceLoader mNetworkResourceLoader;
	private final IHtmlCaptchaChecker mHtmlCaptchaChecker;
	private final DefaultHttpClient mHttpClient;
	
	private boolean mCanSkip = false;
	private CaptchaEntity mCaptcha;
	private Bitmap mCaptchaImage;
	private String mUserError;
	
	public DownloadCaptchaTask(ICaptchaView view, String board, String threadNumber, IJsonApiReader jsonReader, INetworkResourceLoader networkResourceLoader, IHtmlCaptchaChecker htmlCaptchaChecker, DefaultHttpClient httpClient) {
		this.mView = view;
		this.mJsonReader = jsonReader;
		this.mBoard = board;
		this.mThreadNumberd = threadNumber;
		this.mNetworkResourceLoader = networkResourceLoader;
		this.mHtmlCaptchaChecker = htmlCaptchaChecker;
		this.mHttpClient = httpClient;
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
		this.mCanSkip = this.mHtmlCaptchaChecker.canSkipCaptcha(this.mBoard, this.mThreadNumberd);
		if(this.mCanSkip) return true;
		
		this.mCaptcha = RecaptchaService.loadCaptcha(new HttpStringReader(this.mHttpClient));
		if(this.mCaptcha == null) return false;
		
		if(this.isCancelled()) return false;

		this.mCaptchaImage = this.mNetworkResourceLoader.loadBitmap(Uri.parse(this.mCaptcha.getUrl()));
		
		return true;
	}
}
