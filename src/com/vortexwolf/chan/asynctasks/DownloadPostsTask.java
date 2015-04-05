package com.vortexwolf.chan.asynctasks;

import android.os.AsyncTask;
import android.view.Window;

import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.interfaces.IPostsListView;
import com.vortexwolf.chan.models.domain.PostModel;

public class DownloadPostsTask extends AsyncTask<Integer, Long, Boolean> implements IJsonProgressChangeListener, ICancelled {

    static final String TAG = "DownloadPostsTask";

    private final IJsonApiReader mJsonReader;
    private final IPostsListView mView;
    private final String mThreadNumber;
    private final String mBoard;
    private final boolean mIsPartialLoading;
    private final boolean mIsCheckModified;

    private int mLoadAfterPost = 0;
    private PostModel[] mPostsList = null;
    private String mUserError = null;

    // Progress bar
    private long mContentLength = 0;
    private long mProgressOffset = 0;
    private double mProgressScale = 1;

    public DownloadPostsTask(IPostsListView view, String board, String threadNumber, boolean checkModified, IJsonApiReader jsonReader, boolean isPartialLoading) {
        this.mJsonReader = jsonReader;
        this.mView = view;
        this.mThreadNumber = threadNumber;
        this.mBoard = board;
        this.mIsPartialLoading = isPartialLoading;
        this.mIsCheckModified = checkModified;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {

        if (params.length > 0) {
            this.mLoadAfterPost = params[0];
        }

        // Читаем по ссылке json-объект со списком постов
        try {
            this.mPostsList = this.mJsonReader.readPostsList(this.mBoard, this.mThreadNumber, this.mLoadAfterPost, this.mIsCheckModified, this, this);
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
    }

    @Override
    public void onPostExecute(Boolean success) {
        // Прячем все индикаторы загрузки
        if (this.mIsPartialLoading) {
            this.mView.hideUpdateLoading();
        } else {
            this.mView.hideLoadingScreen();
        }

        // Обновляем список или отображаем ошибку
        if (success) {
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

    @Override
    public void onProgressUpdate(Long... progress) {
        // 0-9999 is ok, 10000 means it's finished
        if (this.mContentLength > 0) {
            double relativeProgress = progress[0].longValue() / (double) this.mContentLength;
            this.mView.setWindowProgress((int) (relativeProgress * 9999));
        }
    }

    @Override
    public void progressChanged(long newValue) {
        if (this.isCancelled()) {
            return;
        }

        long absoluteProgress = this.mProgressOffset + (long) (newValue * this.mProgressScale);
        this.publishProgress(absoluteProgress);
    }

    @Override
    public void indeterminateProgress() {
        this.mView.setWindowProgress(Window.PROGRESS_INDETERMINATE_ON);
    }

    @Override
    public void setContentLength(long value) {
        this.mContentLength = value;
    }

    @Override
    public long getContentLength() {
        return this.mContentLength;
    }

    @Override
    public void setOffsetAndScale(long offset, double scale) {
        this.mProgressOffset = offset;
        this.mProgressScale = scale;
    }
}
