package com.vortexwolf.dvach.asynctasks;

import android.os.AsyncTask;

import com.vortexwolf.dvach.interfaces.IPostSendView;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.models.domain.PostEntity;
import com.vortexwolf.dvach.models.domain.PostFields;

public class SendPostTask extends AsyncTask<Void, Long, Boolean> {

    private final IPostSender mPostSender;
    private final IPostSendView mView;

    private final String mBoardName;
    private final String mThreadNumber;
    private final PostEntity mEntity;

    private String mRedirectedPage = null;
    private String mUserError;

    public SendPostTask(IPostSender postSender, IPostSendView view, String boardName, String threadNumber, PostEntity entity) {
        this.mPostSender = postSender;
        this.mView = view;

        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mEntity = entity;
    }

    @Override
    protected Boolean doInBackground(Void... args) {
        try {
            this.mRedirectedPage = this.mPostSender.sendPost(this.mBoardName, this.mThreadNumber, this.getPostFields(), this.mEntity);
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

    private PostFields getPostFields() {
        return PostFields.getDefault();
    }
}
