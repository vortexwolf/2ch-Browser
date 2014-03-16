package com.vortexwolf.dvach.asynctasks;

import java.io.File;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;

import com.vortexwolf.chan.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.library.BackgroundDownloadFileView;
import com.vortexwolf.dvach.common.library.DialogDownloadFileView;
import com.vortexwolf.dvach.common.library.SingleMediaScanner;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.exceptions.DownloadFileException;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IDownloadFileView;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.services.domain.DownloadFileService;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class DownloadFileTask extends AsyncTask<String, Long, Boolean> implements ICancelled, IProgressChangeListener {
    public static final String TAG = "DownloadFileTask";
    private final DownloadFileService mDownloadFileService;
    private final Context mContext;
    private final Resources mResources;
    private final Uri mFrom;
    private final ApplicationSettings mSettings;
    private final IDownloadFileView mProgressView;
    private final ICacheDirectoryManager mCacheDirectoryManager;
    private final boolean mUpdateGallery;

    private File mSaveTo;
    private String mUserError = null;
    private long mContentLength = 0;

    {
        this.mDownloadFileService = Factory.getContainer().resolve(DownloadFileService.class);
        this.mSettings = Factory.getContainer().resolve(ApplicationSettings.class);
        this.mCacheDirectoryManager = Factory.getContainer().resolve(ICacheDirectoryManager.class);
    }

    public DownloadFileTask(Context context, Uri from) {
        this(context, from, null, null, true);
    }

    public DownloadFileTask(Context context, Uri from, File to, IDownloadFileView progressView, boolean updateGallery) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mFrom = from;
        this.mSaveTo = to != null ? to : IoUtils.getSaveFilePath(this.mFrom, this.mSettings);
        this.mUpdateGallery = updateGallery;

        if (progressView == null) {
            this.mProgressView = this.mSettings.isDownloadInBackground()
                    ? new BackgroundDownloadFileView(this.mContext)
                    : new DialogDownloadFileView(this.mContext);
        } else {
            this.mProgressView = progressView;
        }

        this.mProgressView.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                DownloadFileTask.this.cancel(true);
            }
        });
    }

    @Override
    protected Boolean doInBackground(String... arg0) {
        try {
            Uri from = this.getSaveFromUri();

            this.mDownloadFileService.downloadFile(from, this.mSaveTo, this, this);

            return true;
        } catch (DownloadFileException e) {
            this.mUserError = e.getMessage();
            return false;
        }
    }

    @Override
    public void onPreExecute() {
        // Не показывать диалог совсем, если файл существует
        if (this.mSaveTo.exists()) {
            this.cancel(false);
            this.mProgressView.showFileExists(this.mSaveTo);

            return;
        }

        this.mProgressView.showLoading(this.mContext.getString(R.string.notification_save_image_started, this.mSaveTo.getAbsolutePath()));
    }

    @Override
    public void onPostExecute(Boolean success) {
        this.mProgressView.hideLoading();

        if (success) {
            if (this.mUpdateGallery) {
                new SingleMediaScanner(this.mContext, this.mSaveTo);
            }
            this.mProgressView.showSuccess(this.mSaveTo);
        } else {
            this.mProgressView.showError(this.mUserError);
        }
    }

    private Uri getSaveFromUri() {
        Uri from = this.mFrom;

        File cachedFile = this.mCacheDirectoryManager.getCachedImageFileForRead(from);
        if (cachedFile.exists()) {
            from = Uri.fromFile(cachedFile);
        }

        return from;
    }

    @Override
    public void onProgressUpdate(Long... progress) {
        this.mProgressView.setCurrentProgress(progress[0].intValue());
    }

    @Override
    public void progressChanged(long newValue) {
        this.publishProgress(newValue / 1024);
    }

    @Override
    public void indeterminateProgress() {
        // nothing
    }

    @Override
    public void setContentLength(long value) {
        this.mContentLength = value;
        this.mProgressView.setMaxProgress((int) value / 1024);
    }
    
    @Override
    public long getContentLength() {
        return this.mContentLength;
    }
}
