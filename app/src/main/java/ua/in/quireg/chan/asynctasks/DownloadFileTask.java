package ua.in.quireg.chan.asynctasks;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.BackgroundDownloadFileView;
import ua.in.quireg.chan.common.library.DialogDownloadFileView;
import ua.in.quireg.chan.common.library.SingleMediaScanner;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.exceptions.DownloadFileException;
import ua.in.quireg.chan.interfaces.ICancelled;
import ua.in.quireg.chan.interfaces.ICloudflareCheckListener;
import ua.in.quireg.chan.interfaces.IDownloadFileView;
import ua.in.quireg.chan.interfaces.IProgressChangeListener;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.CloudflareCheckService;
import ua.in.quireg.chan.services.http.DownloadFileService;
import ua.in.quireg.chan.settings.ApplicationSettings;

import java.io.File;

public class DownloadFileTask extends AsyncTask<String, Long, Boolean> implements ICancelled, IProgressChangeListener {

    private final DownloadFileService mDownloadFileService;
    private final Activity mActivity;
    private final Resources mResources;
    private final Uri mFrom;
    private final ApplicationSettings mSettings;
    private final IDownloadFileView mProgressView;
    private final CacheDirectoryManager mCacheDirectoryManager;
    private final boolean mUpdateGallery;
    private boolean retry = true; //after cloudflare check

    private File mSaveTo;
    private String mUserError = null;
    private long mContentLength = 0;

    {
        mDownloadFileService = Factory.getContainer().resolve(DownloadFileService.class);
        mSettings = Factory.getContainer().resolve(ApplicationSettings.class);
        mCacheDirectoryManager = Factory.getContainer().resolve(CacheDirectoryManager.class);
    }

    public DownloadFileTask(Activity context, Uri from) {
        this(context, from, null, null, true);
    }

    public DownloadFileTask(Activity context, Uri from, File to, IDownloadFileView progressView, boolean updateGallery) {
        mActivity = context;
        mResources = context.getResources();
        mFrom = from;
        mSaveTo = to != null ? to : IoUtils.getSaveFilePath(mFrom, mSettings);
        mUpdateGallery = updateGallery;

        if (progressView == null) {
            mProgressView = mSettings.isDownloadInBackground()
                    ? new BackgroundDownloadFileView(mActivity)
                    : new DialogDownloadFileView(mActivity);
        } else {
            mProgressView = progressView;
        }

        mProgressView.setOnCancelListener(dialog -> {
            DownloadFileTask.this.retry = false;
            DownloadFileTask.this.cancel(true);
        });
    }

    @Override
    protected Boolean doInBackground(String... arg0) {
        try {
            Uri from = getSaveFromUri();

            mDownloadFileService.downloadFile(from, mSaveTo, this, this);

            return true;
        } catch (DownloadFileException e) {
            mUserError = e.getMessage();
            return false;
        }
    }

    @Override
    public void onPreExecute() {
        // Не показывать диалог совсем, если файл существует
        if (mSaveTo.exists()) {
            cancel(false);
            mProgressView.showFileExists(mSaveTo);

            return;
        }

        mProgressView.showLoading(mResources.getString(R.string.notification_save_image_started, mSaveTo.getAbsolutePath()));
    }

    @Override
    public void onPostExecute(Boolean success) {

        if (success) {
            Uri uri = Uri.fromFile(mSaveTo);
            if (mUpdateGallery && (UriUtils.isImageUri(uri) || UriUtils.isVideoUri(uri))) {
                SingleMediaScanner scanner = new SingleMediaScanner(mActivity, mSaveTo);
                scanner.scan();
            }
            mProgressView.hideLoading();
            mProgressView.showSuccess(mSaveTo);
        } else {
            if (mUserError.equals("503")) {
                String url = DownloadFileTask.this.mFrom.toString();
                new CloudflareCheckService(url, /*Activity*/ mActivity, new ICloudflareCheckListener(){
                    public void onTimeout() {
                        DownloadFileTask.this.mProgressView.hideLoading();
                        DownloadFileTask.this.mProgressView.showError(mResources.getString(R.string.error_cloudflare_check_timeout));
                    }
                    public void onSuccess() {
                        if (DownloadFileTask.this.retry)
                            new DownloadFileTask(
                                DownloadFileTask.this.mActivity,
                                DownloadFileTask.this.mFrom,
                                null,
                                DownloadFileTask.this.mProgressView,
                                true).execute();
                    }
                    public void onStart() {
                        DownloadFileTask.this.mProgressView.showError(mResources.getString(R.string.notification_cloudflare_check_started));
                    }
                }).start();
            } else {
                mProgressView.hideLoading();
                mProgressView.showError(mUserError);
            }
        }
    }

    private Uri getSaveFromUri() {
        Uri from = mFrom;

        File cachedFile = mCacheDirectoryManager.getCachedMediaFileForRead(from);
        if (cachedFile.exists()) {
            from = Uri.fromFile(cachedFile);
        }

        return from;
    }

    @Override
    public void onProgressUpdate(Long... progress) {
        mProgressView.setCurrentProgress(progress[0].intValue());
    }

    @Override
    public void progressChanged(long newValue) {
        publishProgress(newValue / 1024);
    }

    @Override
    public void indeterminateProgress() {
        // nothing
    }

    @Override
    public void setContentLength(long value) {
        mContentLength = value;
        mProgressView.setMaxProgress((int) value / 1024);
    }

    @Override
    public long getContentLength() {
        return mContentLength;
    }
}
