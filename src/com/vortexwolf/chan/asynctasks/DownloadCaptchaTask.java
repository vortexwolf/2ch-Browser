package com.vortexwolf.chan.asynctasks;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.ICaptchaView;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.services.HtmlCaptchaChecker;
import com.vortexwolf.chan.services.RecaptchaService;
import com.vortexwolf.chan.services.YandexCaptchaService;
import com.vortexwolf.chan.services.http.HttpBitmapReader;

public class DownloadCaptchaTask extends AsyncTask<String, Void, Boolean> implements ICancelled {
    private static final String TAG = "DownloadCaptchaTask";

    private final ICaptchaView mView;
    private final String mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;
    private final HttpBitmapReader mHttpBitmapReader;
    private final HtmlCaptchaChecker mHtmlCaptchaChecker;
    private final boolean mCfRecaptcha;

    private boolean mCanSkip = false;
    private boolean mSuccessPasscode = false;
    private boolean mFailPasscode = false;
    private CaptchaEntity mCaptcha;
    private Bitmap mCaptchaImage;
    private String mUserError;

    public DownloadCaptchaTask(ICaptchaView view, String website, String boardName, String threadNumber, boolean isCfRecaptcha) {
        this.mView = view;
        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mHttpBitmapReader = Factory.resolve(HttpBitmapReader.class);
        this.mHtmlCaptchaChecker = Factory.resolve(HtmlCaptchaChecker.class);
        this.mCfRecaptcha = isCfRecaptcha;
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
        if (this.mCfRecaptcha) {
            this.mCaptcha = RecaptchaService.loadCloudflareCaptcha();
        } else {
            HtmlCaptchaChecker.CaptchaResult result = this.mHtmlCaptchaChecker.canSkipCaptcha(this.mWebsite, this.mBoardName, this.mThreadNumber);
            this.mCanSkip = result.canSkip;
            this.mSuccessPasscode = result.successPassCode;
            this.mFailPasscode = result.failPassCode;
            String captchaKey = result.captchaKey;

            if (this.mSuccessPasscode || this.mFailPasscode || this.mCanSkip && !StringUtils.isEmpty(this.mThreadNumber)) {
                return true;
            }

            if (captchaKey != null) {
                this.mCaptcha = YandexCaptchaService.loadCaptcha(captchaKey);
            } else {
                this.mCaptcha = RecaptchaService.loadPostingRecaptcha();
            }
        }

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
