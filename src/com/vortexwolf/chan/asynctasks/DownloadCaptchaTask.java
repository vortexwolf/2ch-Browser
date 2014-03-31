package com.vortexwolf.chan.asynctasks;

import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.vortexwolf.chan.boards.dvach.DvachUriParser;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.ICaptchaView;
import com.vortexwolf.chan.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.services.HtmlCaptchaChecker;
import com.vortexwolf.chan.services.YandexCaptchaService;
import com.vortexwolf.chan.services.http.HttpBitmapReader;

public class DownloadCaptchaTask extends AsyncTask<String, Void, Boolean> implements ICancelled {
    private static final String TAG = "DownloadCaptchaTask";

    private final ICaptchaView mView;
    private final IJsonApiReader mJsonReader;
    private final Uri mRefererUri;
    private final HttpBitmapReader mHttpBitmapReader;
    private final IHtmlCaptchaChecker mHtmlCaptchaChecker;
    private final DefaultHttpClient mHttpClient;
    private final DvachUriParser mUriParser;

    private boolean mCanSkip = false;
    private boolean mSuccessPasscode = false;
    private boolean mFailPasscode = false;
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
        this.mUriParser = Factory.resolve(DvachUriParser.class);
    }

    @Override
    public void onPreExecute() {
        this.mView.showCaptchaLoading();
    }

    @Override
    public void onPostExecute(Boolean success) {
        if (this.mCanSkip) {
            this.mView.skipCaptcha(this.mSuccessPasscode, this.mFailPasscode);
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
        this.mSuccessPasscode = result.successPassCode;
        this.mFailPasscode = result.failPassCode;
        String captchaKey = result.captchaKey;

        if (this.mSuccessPasscode || this.mFailPasscode || this.mCanSkip && !this.mUriParser.isBoardUri(this.mRefererUri)) {
            return true;
        }
        if (captchaKey == null) {
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
