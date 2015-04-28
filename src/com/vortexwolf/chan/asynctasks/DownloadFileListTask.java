package com.vortexwolf.chan.asynctasks;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.BackgroundDownloadFileView;
import com.vortexwolf.chan.common.library.DialogDownloadFileView;
import com.vortexwolf.chan.common.library.ListMediaScanner;
import com.vortexwolf.chan.common.library.SingleMediaScanner;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.exceptions.DownloadFileException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.ICloudflareCheckListener;
import com.vortexwolf.chan.interfaces.IDownloadFileView;
import com.vortexwolf.chan.interfaces.IProgressChangeListener;
import com.vortexwolf.chan.services.CacheDirectoryManager;
import com.vortexwolf.chan.services.CloudflareCheckService;
import com.vortexwolf.chan.services.http.DownloadFileService;
import com.vortexwolf.chan.settings.ApplicationSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadFileListTask extends AsyncTask<Void, Long, Integer> implements ICancelled {
    private final DownloadFileService mDownloadFileService = Factory.resolve(DownloadFileService.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private final CacheDirectoryManager mCacheDirectoryManager = Factory.resolve(CacheDirectoryManager.class);
    private final Context mContext;
    private final List<String> mFromList;

    private String mUserError = null;
    private ArrayList<String> mDownloadedFilePaths = new ArrayList<String>();

    public DownloadFileListTask(Context context, List<String> fromList) {
        this.mContext = context;
        this.mFromList = fromList;
    }

    @Override
    protected Integer doInBackground(Void... arg0) {
        for (String from : this.mFromList) {
            Uri saveFrom = this.getUriOrCachedFile(Uri.parse(from));
            File saveTo = IoUtils.getSaveFilePath(saveFrom, this.mSettings);
            if (saveTo.exists()) {
                continue;
            }

            try {
                this.mDownloadFileService.downloadFile(saveFrom, saveTo, null, this);
                this.mDownloadedFilePaths.add(saveTo.getAbsolutePath());
            } catch (DownloadFileException e) {
                this.mUserError = e.getMessage();
                continue;
            }
        }

        return this.mDownloadedFilePaths.size();
    }

    @Override
    public void onPreExecute() {
        AppearanceUtils.showToastMessage(this.mContext,
            this.mContext.getResources().getQuantityString(R.plurals.data_files_download_start_quantity,
                this.mFromList.size(), this.mFromList.size(), this.mSettings.getDownloadDirectory()));
    }

    @Override
    public void onPostExecute(Integer downloadedCount) {
        if (downloadedCount > 0) {
            ArrayList<String> downloadedImagePaths = new ArrayList<String>();
            for (String path : this.mDownloadedFilePaths) {
                if (RegexUtils.isImagePathString(path)) {
                    downloadedImagePaths.add(path);
                }
            }

            ListMediaScanner scanner = new ListMediaScanner(this.mContext, downloadedImagePaths.toArray(new String[downloadedImagePaths.size()]));
            scanner.scan();

            AppearanceUtils.showToastMessage(this.mContext, this.mContext.getResources()
                    .getQuantityString(R.plurals.data_downloaded_files_quantity, downloadedCount, downloadedCount));
        } else {
            AppearanceUtils.showToastMessage(this.mContext,
                    !StringUtils.isEmpty(this.mUserError)
                            ? this.mUserError
                            : this.mContext.getResources().getQuantityString(R.plurals.data_downloaded_files_quantity, 0, 0));
        }
    }

    private Uri getUriOrCachedFile(Uri from) {
        File cachedFile = this.mCacheDirectoryManager.getCachedImageFileForRead(from);
        if (cachedFile.exists()) {
            return Uri.fromFile(cachedFile);
        }

        return from;
    }
}
