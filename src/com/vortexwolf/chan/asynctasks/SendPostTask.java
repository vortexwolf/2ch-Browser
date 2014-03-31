package com.vortexwolf.chan.asynctasks;

import android.os.AsyncTask;

import com.vortexwolf.chan.interfaces.IPostSendView;
import com.vortexwolf.chan.interfaces.IPostSender;
import com.vortexwolf.chan.models.domain.SendPostModel;

public class SendPostTask extends AsyncTask<Void, Long, Boolean> {

    private final IPostSender mPostSender;
    private final IPostSendView mView;

    private final String mBoardName;
    private final String mThreadNumber;
    private final SendPostModel mEntity;

    private String mRedirectedPage = null;
    private String mUserError;

    public SendPostTask(IPostSender postSender, IPostSendView view, String boardName, String threadNumber, SendPostModel entity) {
        this.mPostSender = postSender;
        this.mView = view;

        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mEntity = entity;
    }

    @Override
    protected Boolean doInBackground(Void... args) {
        try {
            this.mRedirectedPage = this.mPostSender.sendPost(this.mBoardName, this.mEntity);
            return true;
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
            this.mView.showError(this.mUserError);
        }
    }
}
