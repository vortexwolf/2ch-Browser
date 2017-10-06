package ua.in.quireg.chan.asynctasks;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.exceptions.HttpRequestException;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.services.RecaptchaService;
import ua.in.quireg.chan.services.http.HttpBitmapReader;

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
        this.mRecaptcha = RecaptchaService.loadCloudflareCaptcha();
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
