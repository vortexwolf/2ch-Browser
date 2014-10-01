package com.vortexwolf.chan.asynctasks;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.services.RecaptchaService;
import com.vortexwolf.chan.services.http.HttpBitmapReader;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class DownloadRecaptchaTask extends AsyncTask<Void, Void, Boolean> {
    public static final String TAG = "DownloadRecaptchaTask";
    private static final HttpBitmapReader mHttpBitmapReader = Factory.resolve(HttpBitmapReader.class);

    private CaptchaEntity mRecaptcha = null;
    private Bitmap mBitmap = null;
    private String mUserError = null;

    @Override
    public void onPreExecute() {
        // this.mView.showCaptchaLoading();
    }

    @Override
    public void onPostExecute(Boolean success) {
        if (success) {
            // TODO: implement
            // this.mView.showCaptcha(this.mRecaptcha, this.mBitmap);
        } else {
            // this.mView.showCaptchaError(this.mUserError);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        this.mRecaptcha = RecaptchaService.loadCaptcha();
        if (this.mRecaptcha == null) {
            this.mUserError = "Error while loading the captcha page.";
            return false;
        }

        try {
            this.mBitmap = mHttpBitmapReader.fromUri(this.mRecaptcha.getUrl());
        } catch (HttpRequestException e) {
            MyLog.e(TAG, e);
            this.mUserError = e.getMessage();
            return false;
        }

        return true;
    }

}
