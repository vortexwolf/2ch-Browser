package com.vortexwolf.dvach.asynctasks;

import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.exceptions.HttpRequestException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.ICaptchaView;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;
import com.vortexwolf.dvach.services.domain.HtmlCaptchaChecker;
import com.vortexwolf.dvach.services.domain.HttpBitmapReader;
import com.vortexwolf.dvach.services.domain.YandexCaptchaService;

public class DownloadCaptchaTask extends AsyncTask<String, Void, Boolean> implements ICancelled {
    private static final String TAG = "DownloadCaptchaTask";

    private final ICaptchaView mView;
    private final IJsonApiReader mJsonReader;
    private final Uri mRefererUri;
    private final HttpBitmapReader mHttpBitmapReader;
    private final IHtmlCaptchaChecker mHtmlCaptchaChecker;
    private final DefaultHttpClient mHttpClient;

    private boolean mCanSkip = false;
    private boolean mSkipPasscode = false;
    private CaptchaEntity mCaptcha;
    private Bitmap mCaptchaImage;
    private String mUserError;

    public DownloadCaptchaTask(ICaptchaView view, Uri refererUri, IJsonApiReader jsonReader, HttpBitmapReader httpBitmapReader, IHtmlCaptchaChecker htmlCaptchaChecker, DefaultHttpClient httpClient) {
        this.mView = view;
        this.mJsonReader = jsonReader;
        this.mRefererUri = refererUri;
        this.mHttpBitmapReader = httpBitmapReader;
        this.mHtmlCaptchaChecker = htmlCaptchaChecker;
        this.mHttpClient = httpClient;
    }

    @Override
    public void onPreExecute() {
        this.mView.showCaptchaLoading();
    }

    @Override
    public void onPostExecute(Boolean success) {
        if (this.mCanSkip) {
            this.mView.skipCaptcha(this.mSkipPasscode);
        } else if (success && this.mCaptcha != null) {
            this.mView.showCaptcha(this.mCaptcha, this.mCaptchaImage);
        } else {
            this.mView.showCaptchaError(this.mUserError);
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        HtmlCaptchaChecker.CaptchaResult result = this.mHtmlCaptchaChecker.canSkipCaptcha(this.mRefererUri, true);
        this.mCanSkip = result.canSkip;
        this.mSkipPasscode = result.passCode;
        String captchaKey = result.captchaKey;

        if (this.mSkipPasscode || this.mCanSkip && !UriUtils.isBoardUri(this.mRefererUri)) {
            return true;
        }
        if(captchaKey == null) {
            return false;
        }

        this.mCaptcha = YandexCaptchaService.loadCaptcha(captchaKey);

        if (this.isCancelled()) {
            return false;
        }

        try {
            this.mCaptchaImage = this.mHttpBitmapReader.fromUri(this.mCaptcha.getUrl());
        } catch (HttpRequestException e) {
            this.mUserError = e.getMessage();
            return false;
        }

        return true;
    }
}
