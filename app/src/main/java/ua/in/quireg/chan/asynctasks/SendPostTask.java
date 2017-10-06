package ua.in.quireg.chan.asynctasks;

import android.os.AsyncTask;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.interfaces.ICaptchaView;
import ua.in.quireg.chan.interfaces.IPostSendView;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.SendPostModel;
import ua.in.quireg.chan.models.domain.SendPostResult;
import ua.in.quireg.chan.services.PostSender;

public class SendPostTask extends AsyncTask<Void, Long, SendPostResult> {

    private final PostSender mPostSender = Factory.resolve(PostSender.class);
    private final IPostSendView mView;
    private final ICaptchaView mCaptchaView;

    private final IWebsite mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;
    private final SendPostModel mEntity;

    public SendPostTask(IPostSendView view, ICaptchaView captchaView, IWebsite website, String boardName, String threadNumber, SendPostModel entity) {
        this.mView = view;
        this.mCaptchaView = captchaView;

        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mEntity = entity;
    }

    @Override
    protected SendPostResult doInBackground(Void... args) {
        SendPostResult result = this.mPostSender.sendPost(this.mWebsite, this.mBoardName, this.mEntity);
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
            this.mView.showError(result.error, result.isRecaptcha);
        }
    }
}
