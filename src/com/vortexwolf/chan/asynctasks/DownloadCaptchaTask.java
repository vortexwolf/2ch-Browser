package com.vortexwolf.chan.asynctasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.ICaptchaView;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.CaptchaType;
import com.vortexwolf.chan.services.DvachCaptchaService;
import com.vortexwolf.chan.services.HtmlCaptchaChecker;
import com.vortexwolf.chan.services.MailruCaptchaService;
import com.vortexwolf.chan.services.RecaptchaService;
import com.vortexwolf.chan.services.http.HttpBitmapReader;

public class DownloadCaptchaTask extends AsyncTask<String, Void, Boolean> implements ICancelled {
    private static final String TAG = "DownloadCaptchaTask";

    private final ICaptchaView mView;
    private final IWebsite mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;
    private final HttpBitmapReader mHttpBitmapReader;
    private final HtmlCaptchaChecker mHtmlCaptchaChecker;
    //private final CaptchaType mCaptchaType;
    private CaptchaType mCaptchaType;
    private final MailruCaptchaService mMailruCaptchaService;
    private final DvachCaptchaService mDvachCaptchaService;

    private boolean mCanSkip = false;
    private boolean mSuccessPasscode = false;
    private boolean mFailPasscode = false;
    private CaptchaEntity mCaptcha;
    private Bitmap mCaptchaImage;
    private String mUserError;

    public DownloadCaptchaTask(ICaptchaView view, IWebsite website, String boardName, String threadNumber, CaptchaType captchaType) {
        this.mView = view;
        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mHttpBitmapReader = Factory.resolve(HttpBitmapReader.class);
        this.mHtmlCaptchaChecker = Factory.resolve(HtmlCaptchaChecker.class);
        this.mMailruCaptchaService = Factory.resolve(MailruCaptchaService.class);
        this.mDvachCaptchaService = Factory.resolve(DvachCaptchaService.class);
        this.mCaptchaType = captchaType;
    }

    @Override
    public void onPreExecute() {
        this.mView.showCaptchaLoading();
    }

    @Override
    public void onPostExecute(Boolean success) {
        if (this.mCaptchaType == CaptchaType.APP) {
            this.mView.appCaptcha(this.mCaptcha);
        } else if (this.mCanSkip) {
            this.mView.skipCaptcha(this.mSuccessPasscode, this.mFailPasscode);
        } else if (success && this.mCaptcha != null) {
            this.mView.showCaptcha(this.mCaptcha, this.mCaptchaImage);
        } else {
            this.mView.showCaptchaError(this.mUserError);
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String referer = UriUtils.getBoardOrThreadUrl(this.mWebsite.getUrlBuilder(), this.mBoardName, 0, this.mThreadNumber);

        //если это не новый тред проверим апкаптчу и если она работает вернем качпу с кодом
        if (!StringUtils.isEmpty(this.mThreadNumber)) {
            HtmlCaptchaChecker.CaptchaResult acresult =
                this.mHtmlCaptchaChecker.canSkipCaptcha(this.mWebsite, CaptchaType.APP, this.mBoardName, this.mThreadNumber);
            if (!StringUtils.isEmpty(acresult.captchaKey)) {
                this.mCaptchaType = CaptchaType.APP;
                this.mCaptcha = new CaptchaEntity();
                mCaptcha.setKey(acresult.captchaKey);
                mCaptcha.setCaptchaType(CaptchaType.APP);

                return true;
            }
        }
        //иначе используем обычную капчу
        HtmlCaptchaChecker.CaptchaResult result =
            this.mHtmlCaptchaChecker.canSkipCaptcha(this.mWebsite, this.mCaptchaType, this.mBoardName, this.mThreadNumber);
        this.mCanSkip = result.canSkip;
        this.mSuccessPasscode = result.successPassCode;
        this.mFailPasscode = result.failPassCode;
        String captchaKey = result.captchaKey;

        if (this.mSuccessPasscode || this.mFailPasscode || this.mCanSkip && !StringUtils.isEmpty(this.mThreadNumber)) {
            return true;
        }

        if (this.mCaptchaType == CaptchaType.RECAPTCHA_V2) {
            this.mCaptcha = new CaptchaEntity();
            this.mCaptcha.setCaptchaType(this.mCaptchaType);
            // the entity is empty for Recaptcha V2
        } else if (this.mCaptchaType == CaptchaType.RECAPTCHA_V1) {
            this.mCaptcha = RecaptchaService.loadPostingRecaptcha(captchaKey, referer);
        } else if (this.mCaptchaType == CaptchaType.MAILRU) {
            this.mCaptcha = this.mMailruCaptchaService.loadCaptcha(captchaKey, referer);
        } else if (this.mCaptchaType == CaptchaType.DVACH) {
            this.mCaptcha = this.mDvachCaptchaService.loadCaptcha(captchaKey, this.mWebsite);
        } else {
            return false;
        }

        if (this.isCancelled() || this.mCaptcha == null) {
            return false;
        }

        this.mCaptcha.setCaptchaType(this.mCaptchaType);
        if (this.mCaptcha.isError()) {
            this.mUserError = this.mCaptcha.getErrorMessage();
            return false;
        }

        try {
            if (this.mCaptcha.getUrl() != null) {
                this.mCaptchaImage = this.mHttpBitmapReader.fromUri(this.mCaptcha.getUrl());
            }
        } catch (HttpRequestException e) {
            this.mUserError = e.getMessage();
            return false;
        }

        return true;
    }
}
