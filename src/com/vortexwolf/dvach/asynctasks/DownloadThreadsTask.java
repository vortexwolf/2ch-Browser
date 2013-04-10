package com.vortexwolf.dvach.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.Window;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.dvach.interfaces.IListView;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.models.domain.ThreadsList;

public class DownloadThreadsTask extends AsyncTask<Void, Long, Boolean> implements IJsonProgressChangeListener, ICancelled {

    static final String TAG = "DownloadThreadsTask";

    private final Activity mActivity;
    private final IJsonApiReader mJsonReader;
    private final IListView<ThreadsList> mView;
    private final String mBoard;
    private final int mPageNumber;
    private final boolean mIsCheckModified;

    private ThreadsList mThreadsList = null;
    private String mUserError = null;
    // Progress bar
    private long mContentLength = 0;
    private long mProgressOffset = 0;
    private double mProgressScale = 1;
    
    public DownloadThreadsTask(Activity activity, IListView<ThreadsList> view, String board, int pageNumber, boolean checkModified, IJsonApiReader jsonReader) {
        this.mActivity = activity;
        this.mJsonReader = jsonReader;
        this.mView = view;
        this.mBoard = board != null ? board : Constants.DEFAULT_BOARD;
        this.mPageNumber = pageNumber;
        this.mIsCheckModified = checkModified;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // Читаем по ссылке json-объект со списком тредов
        try {
            this.mThreadsList = this.mJsonReader.readThreadsList(this.mBoard, this.mPageNumber, this.mIsCheckModified, this, this);
            return true;
        } catch (Exception e) {
            this.mUserError = e.getMessage();
        }

        return false;
    }

    @Override
    public void onPreExecute() {
        // Отображаем экран загрузки и запускаем прогресс бар
        this.mView.showLoadingScreen();
    }

    @Override
    public void onPostExecute(Boolean success) {
        // Прячем все индикаторы загрузки
        this.mView.hideLoadingScreen();

        // Обновляем список или отображаем ошибку
        if (success && this.mThreadsList != null) {
            this.mView.setData(this.mThreadsList);
        } else if (!success) {
            this.mView.showError(this.mUserError);
        }
    }

    @Override
    public void onProgressUpdate(Long... progress) {
        // 0-9999 is ok, 10000 means it's finished
        if (this.mContentLength > 0) {
            long absoluteProgress = this.mProgressOffset + (long)(progress[0].longValue() * this.mProgressScale);
            double relativeProgress = absoluteProgress / (double)this.mContentLength;
            MyLog.d(TAG, "progress: " + relativeProgress);
            this.mView.setWindowProgress((int)(relativeProgress * 9999));
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
