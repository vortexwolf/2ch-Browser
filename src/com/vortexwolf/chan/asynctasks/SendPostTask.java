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
import com.vortexwolf.chan.models.domain.SendPostResult;
import com.vortexwolf.chan.services.RecaptchaService;

public class SendPostTask extends AsyncTask<Void, Long, SendPostResult> {

    private final IPostSender mPostSender;
    private final IPostSendView mView;
    private final ICaptchaView mCaptchaView;

    private final String mBoardName;
    private final String mThreadNumber;
    private final SendPostModel mEntity;

    public SendPostTask(IPostSender postSender, IPostSendView view, ICaptchaView captchaView, String boardName, String threadNumber, SendPostModel entity) {
        this.mPostSender = postSender;
        this.mView = view;
        this.mCaptchaView = captchaView;

        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mEntity = entity;
    }

    @Override
    protected SendPostResult doInBackground(Void... args) {
        SendPostResult result = this.mPostSender.sendPost(this.mBoardName, this.mEntity);
        return result;
    }

    @Override
    public void onPreExecute() {
        this.mView.showPostLoading();
    }

    @Override
    protected void onPostExecute(final SendPostResult result) {
        this.mView.hidePostLoading();
        if (result.isSuccess) {
            this.mView.showSuccess(result.location);
        } else {            
            this.mView.showError(result.error);
        }
    }
}
