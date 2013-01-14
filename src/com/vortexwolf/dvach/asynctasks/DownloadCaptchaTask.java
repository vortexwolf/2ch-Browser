package com.vortexwolf.dvach.asynctasks;

import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.ICaptchaView;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.INetworkResourceLoader;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;
import com.vortexwolf.dvach.services.domain.HtmlCaptchaChecker;
import com.vortexwolf.dvach.services.domain.HttpStringReader;
import com.vortexwolf.dvach.services.domain.RecaptchaService;
import com.vortexwolf.dvach.services.domain.SolvemediaCaptchaService;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

public class DownloadCaptchaTask extends AsyncTask<String, Void, Boolean> implements ICancelled {
    private static final String TAG = "DownloadCaptchaTask";

    private final ICaptchaView mView;
    private final IJsonApiReader mJsonReader;
    private final Uri mRefererUri;
    private final INetworkResourceLoader mNetworkResourceLoader;
    private final IHtmlCaptchaChecker mHtmlCaptchaChecker;
    private final DefaultHttpClient mHttpClient;

    private boolean mCanSkip = false;
    private CaptchaEntity mCaptcha;
    private Bitmap mCaptchaImage;
    private String mUserError;

    public DownloadCaptchaTask(ICaptchaView view, Uri refererUri, IJsonApiReader jsonReader, INetworkResourceLoader networkResourceLoader, IHtmlCaptchaChecker htmlCaptchaChecker, DefaultHttpClient httpClient) {
        this.mView = view;
        this.mJsonReader = jsonReader;
        this.mRefererUri = refererUri;
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
        if (this.mCanSkip) {
            this.mView.skipCaptcha();
        } else if (success && mCaptcha != null) {
            this.mView.showCaptcha(mCaptcha, mCaptchaImage);
        } else {
            this.mView.showCaptchaError(this.mUserError);
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String captchaKey = null;
        // check captcha only for new posts, not for new threads
        if (UriUtils.isThreadUri(this.mRefererUri)) {
            HtmlCaptchaChecker.CaptchaResult result = this.mHtmlCaptchaChecker.canSkipCaptcha(this.mRefererUri);
            this.mCanSkip = result.canSkip;
            captchaKey = result.captchaKey;
        }
        if (this.mCanSkip) return true;

        this.mCaptcha = SolvemediaCaptchaService.loadCaptcha(new HttpStringReader(this.mHttpClient), captchaKey);
        if (this.mCaptcha == null) return false;

        if (this.isCancelled()) return false;

        this.mCaptchaImage = this.mNetworkResourceLoader.loadBitmap(Uri.parse(this.mCaptcha.getUrl()));

        return true;
    }
}
