package com.vortexwolf.dvach.asynctasks;

import android.os.AsyncTask;
import android.view.Window;

import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IPostsListView;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.models.domain.PostsList;

public class DownloadPostsTask extends AsyncTask<String, Long, Boolean> implements IProgressChangeListener, ICancelled {

    static final String TAG = "DownloadPostsTask";

    private final IJsonApiReader mJsonReader;
    private final IPostsListView mView;
    private final String mThreadNumber;
    private final String mBoard;
    private final boolean mIsPartialLoading;
    private final boolean mIsCheckModified;

    private String mLoadAfterPost = null;
    private PostsList mPostsList = null;
    private String mUserError = null;

    // Progress bar
    private long mContentLength = 0;

    public DownloadPostsTask(IPostsListView view, String board, String threadNumber, boolean checkModified, IJsonApiReader jsonReader, boolean isPartialLoading) {
        this.mJsonReader = jsonReader;
        this.mView = view;
        this.mThreadNumber = threadNumber;
        this.mBoard = board;
        this.mIsPartialLoading = isPartialLoading;
        this.mIsCheckModified = checkModified;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        if (params.length > 0) {
            this.mLoadAfterPost = params[0];
        } else {
            this.mLoadAfterPost = null;
        }

        // Читаем по ссылке json-объект со списком постов
        try {
            this.mPostsList = this.mJsonReader.readPostsList(this.mBoard, this.mThreadNumber, this.mIsCheckModified, this, this);
            return true;
        } catch (Exception e) {
            this.mUserError = e.getMessage();
        }

        return false;
    }

    @Override
    public void onPreExecute() {
        if (this.mIsPartialLoading) {
            this.mView.showUpdateLoading();
        } else {
            this.mView.showLoadingScreen();
        }

        if (this.mContentLength == -1) {
            this.mView.setWindowProgress(Window.PROGRESS_INDETERMINATE_ON);
        } else {
            this.mView.setWindowProgress(0);
        }
    }

    @Override
    public void onPostExecute(Boolean success) {
        // Прячем все индикаторы загрузки
        this.onFinished();

        // Обновляем список или отображаем ошибку
        if (success && this.mPostsList != null) {
            if (this.mIsPartialLoading) {
                this.mView.updateData(this.mLoadAfterPost, this.mPostsList);
            } else {
                this.mView.setData(this.mPostsList);
            }
        } else if (!success) {
            if (this.mIsPartialLoading) {
                this.mView.showUpdateError(this.mUserError);
            } else {
                this.mView.showError(this.mUserError);
            }
        }

        // else show "No new posts"
    }

    private void onFinished() {
        if (this.mIsPartialLoading) {
            this.mView.hideUpdateLoading();
        } else {
            this.mView.hideLoadingScreen();
        }

        // Hide progress anyway
        this.mView.setWindowProgress(Window.PROGRESS_END);
    }

    @Override
    public void onProgressUpdate(Long... progress) {
        // 0-9999 is ok, 10000 means it's finished
        if (this.mContentLength > 0) {
            int relativeProgress = progress[0].intValue() * 9999 / (int) this.mContentLength;
            this.mView.setWindowProgress(relativeProgress);
        }
    }

    @Override
    public void progressChanged(long newValue) {
        if (this.isCancelled()) {
            return;
        }

        this.publishProgress(newValue);
    }

    @Override
    public void indeterminateProgress() {
        this.mView.setWindowProgress(Window.PROGRESS_INDETERMINATE_ON);
    }

    @Override
    public void setContentLength(long value) {
        this.mContentLength = value;
    }
}
