package com.vortexwolf.chan.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.Window;

import com.vortexwolf.chan.exceptions.HtmlNotJsonException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.interfaces.IListView;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.ThreadModel;
import com.vortexwolf.chan.services.RecaptchaService;

public class DownloadThreadsTask extends AsyncTask<Void, Long, Boolean> implements IJsonProgressChangeListener, ICancelled {

    static final String TAG = "DownloadThreadsTask";

    private final Activity mActivity;
    private final IJsonApiReader mJsonReader;
    private final IListView<ThreadModel[]> mView;
    private final String mBoard;
    private final int mPageNumber;
    private final boolean mIsCheckModified;

    private ThreadModel[] mThreadsList = null;
    private String mUserError = null;
    private CaptchaEntity mRecaptcha = null;
    // Progress bar
    private long mContentLength = 0;
    private long mProgressOffset = 0;
    private double mProgressScale = 1;

    public DownloadThreadsTask(Activity activity, IListView<ThreadModel[]> view, String board, int pageNumber, boolean checkModified, IJsonApiReader jsonReader) {
        this.mActivity = activity;
        this.mJsonReader = jsonReader;
        this.mView = view;
        this.mBoard = board;
        this.mPageNumber = pageNumber;
        this.mIsCheckModified = checkModified;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // Читаем по ссылке json-объект со списком тредов
        try {
            this.mThreadsList = this.mJsonReader.readThreadsList(this.mBoard, this.mPageNumber, this.mIsCheckModified, this, this);
            return true;
        } catch (HtmlNotJsonException he) {
            if (RecaptchaService.isCloudflareCaptchaPage(he.getHtml())) {
                this.mRecaptcha = RecaptchaService.loadCloudflareCaptcha();
            }
            if (this.mRecaptcha == null) {
                this.mUserError = he.getMessage();
            }
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
        if (success) {
            this.mView.setData(this.mThreadsList);
        } else if (!success) {
            if (this.mRecaptcha != null) {
                this.mView.showCaptcha(this.mRecaptcha);
            } else {
                this.mView.showError(this.mUserError);
            }
        }
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
