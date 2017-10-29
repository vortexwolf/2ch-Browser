package ua.in.quireg.chan.asynctasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.ListMediaScanner;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.exceptions.DownloadFileException;
import ua.in.quireg.chan.interfaces.ICancelled;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.http.DownloadFileService;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class DownloadFileListTask extends AsyncTask<Void, Long, Integer> implements ICancelled {
    private final DownloadFileService mDownloadFileService = Factory.resolve(DownloadFileService.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private final CacheDirectoryManager mCacheDirectoryManager = Factory.resolve(CacheDirectoryManager.class);
    private final Context mContext;
    private final File mSaveFolder;
    private final List<String> mFromList;

    private String mUserError = null;
    private ArrayList<File> mDownloadedFiles = new ArrayList<File>();

    public DownloadFileListTask(Context context, IWebsite website, String threadNumber, List<String> fromList) {
        this.mContext = context;
        this.mSaveFolder = new File(this.mSettings.getDownloadDirectory(), website.name() + " " + threadNumber);
        this.mFromList = fromList;
    }

    @Override
    protected Integer doInBackground(Void... arg0) {
        if (!this.mSaveFolder.exists()) {
            this.mSaveFolder.mkdirs();
        }

        for (String from : this.mFromList) {
            Uri saveFrom = this.getUriOrCachedFile(Uri.parse(from));
            File saveTo = new File(this.mSaveFolder, saveFrom.getLastPathSegment());
            if (saveTo.exists()) {
                continue;
            }

            try {
                this.mDownloadFileService.downloadFile(saveFrom, saveTo, null, this);
                this.mDownloadedFiles.add(saveTo);
            } catch (DownloadFileException e) {
                this.mUserError = e.getMessage();
                continue;
            }
        }

        return this.mDownloadedFiles.size();
    }

    @Override
    public void onPreExecute() {
        AppearanceUtils.showToastMessage(this.mContext,
            this.mContext.getResources().getQuantityString(R.plurals.data_files_download_start_quantity,
                this.mFromList.size(), this.mFromList.size(), this.mSaveFolder));
    }

    @Override
    public void onPostExecute(Integer downloadedCount) {
        if (downloadedCount > 0) {
            ArrayList<String> downloadedImagePaths = new ArrayList<String>();
            for (File file : this.mDownloadedFiles) {
                Uri uri = Uri.fromFile(file);
                if (UriUtils.isImageUri(uri) || UriUtils.isVideoUri(uri)) {
                    downloadedImagePaths.add(file.getAbsolutePath());
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
        File cachedFile = this.mCacheDirectoryManager.getCachedMediaFileForRead(from);
        if (cachedFile.exists()) {
            return Uri.fromFile(cachedFile);
        }

        return from;
    }
}
