package com.vortexwolf.chan.asynctasks;

import android.content.res.Resources;
import android.os.AsyncTask;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.interfaces.ICaptchaView;
import com.vortexwolf.chan.interfaces.IPostSendView;
import com.vortexwolf.chan.interfaces.IPostSender;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.SendPostModel;
import com.vortexwolf.chan.services.RecaptchaService;

public class SendPostTask extends AsyncTask<Void, Long, Boolean> {

    private final IPostSender mPostSender;
    private final IPostSendView mView;
    private final ICaptchaView mCaptchaView;

    private final String mBoardName;
    private final String mThreadNumber;
    private final SendPostModel mEntity;

    private String mRedirectedPage = null;
    private String mUserError;
    
    private CaptchaEntity mRecaptcha = null;

    public SendPostTask(IPostSender postSender, IPostSendView view, ICaptchaView captchaView, String boardName, String threadNumber, SendPostModel entity) {
        this.mPostSender = postSender;
        this.mView = view;
        this.mCaptchaView = captchaView;

        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mEntity = entity;
    }

    @Override
    protected Boolean doInBackground(Void... args) {
        try {
            String result = this.mPostSender.sendPost(this.mBoardName, this.mEntity);
            if (result.equals("__recaptcha__")) {
                this.mRecaptcha = RecaptchaService.loadCaptcha();
                this.mUserError = Factory.resolve(Resources.class).getString(R.string.notification_cloudflare_recaptcha);
                return false;
            } else {
                this.mRedirectedPage = result;
                return true;
            }
        } catch (Exception e) {
            this.mUserError = e.getMessage();
            return false;
        }
    }

    @Override
    public void onPreExecute() {
        this.mView.showPostLoading();
    }

    @Override
    protected void onPostExecute(final Boolean result) {

        this.mView.hidePostLoading();
        if (result) {
            this.mView.showSuccess(this.mRedirectedPage);
        } else {
            if (mRecaptcha != null) new DownloadCaptchaTask(mCaptchaView, mRecaptcha).execute();
            this.mView.showError(this.mUserError);
        }
    }
}
