package ua.in.quireg.chan.services;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class CacheDirectoryManager {

    private static final int WORKER_INTERVAL = 60000; // 60 seconds

    private static final String CACHE_DIR_NAME = "cache";
    private static final String CACHE_THUMB_DIR_NAME = "thumbnails";
    private static final String CACHE_PAGES_DIR_NAME = "pages";
    private static final String CACHE_IMAGES_DIR_NAME = "images";

    @Inject protected ApplicationSettings mSettings;

    private final String mPackageName;
    private final File mInternalCacheDir;
    private final File mExternalCacheDir;

    private Handler mHandler;

    public CacheDirectoryManager(Context context) {

        MainApplication.getAppComponent().inject(this);

        mPackageName = context.getPackageName();

        mExternalCacheDir = getExternalCachePath();
        mInternalCacheDir = new File(context.getCacheDir(), CACHE_DIR_NAME);

        HandlerThread mHandlerThread = new HandlerThread("CacheWorkerThread");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.postDelayed(mWorkerRunnable, WORKER_INTERVAL);
    }

    public long getCacheMaxSize() {
        return mSettings.getCacheSize();
    }

    public File getCurrentCacheDirectory() {

        if (mExternalCacheDir != null && extStorageReady()) {
            return mExternalCacheDir;
        } else {
            return mInternalCacheDir;
        }
    }

    public File getThumbnailsCacheDirectory() {
        return getCacheSubdirectory(CACHE_THUMB_DIR_NAME);
    }

    public File getPagesCacheDirectory() {
        return getCacheSubdirectory(CACHE_PAGES_DIR_NAME);
    }

    public File getMediaCacheDirectory() {
        return getCacheSubdirectory(CACHE_IMAGES_DIR_NAME);
    }

    private File getCacheSubdirectory(String subFolder) {
        File dir = new File(getCurrentCacheDirectory(), subFolder);
        if (!dir.exists() && !dir.mkdirs()) {
            Timber.e("Failed to create cache folder %s", subFolder);
        }
        return dir;
    }

    public File getCachedMediaFileForWrite(Uri uri) {
        String fileName = uri.getLastPathSegment();

        return new File(getMediaCacheDirectory(), fileName);
    }

    public File getCachedMediaFileForRead(Uri uri) {
        File cachedFile = getCachedMediaFileForWrite(uri);
        if (!cachedFile.exists()) {
            cachedFile = IoUtils.getSaveFilePath(uri, mSettings);
        }
        return cachedFile;
    }

    private Runnable mWorkerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                trimCacheIfNeeded();
            } finally {
                mHandler.postDelayed(mWorkerRunnable, WORKER_INTERVAL);
            }
        }
    };

    private synchronized void trimCacheIfNeeded() {

        long cacheSize = IoUtils.dirSize(getCurrentCacheDirectory());
        long maxSize = IoUtils.convertMbToBytes(getCacheMaxSize());

        if (cacheSize < maxSize) {
            return;
        }
        Timber.d("Cache size: %.2fMb, cleanup started", IoUtils.convertBytesToMb(cacheSize));

        final long FILE_CACHE_THRESHOLD = Math.round(IoUtils.convertMbToBytes(mSettings.getCacheSize()));
        final float MAX_THUMBNAILS_PART = mSettings.getCacheThumbnailsSize() / 100f;
        final float MAX_MEDIA_PART = mSettings.getCacheMediaSize() / 100f;
        final float MAX_PAGES_PART = mSettings.getCachePagesSize() / 100f;

        long released = 0;

        File pagesCache = getPagesCacheDirectory();
        long pagesCacheSize = IoUtils.dirSize(pagesCache);

        if (pagesCacheSize > FILE_CACHE_THRESHOLD * MAX_PAGES_PART) {
            released += freeSpace(getFilesListToDelete(pagesCache), Math.round(pagesCacheSize - (FILE_CACHE_THRESHOLD * MAX_PAGES_PART)));
        }

        File mediaCache = getMediaCacheDirectory();
        long mediaCacheSize = IoUtils.dirSize(mediaCache);

        if (mediaCacheSize > FILE_CACHE_THRESHOLD * MAX_MEDIA_PART) {
            released += freeSpace(getFilesListToDelete(mediaCache), Math.round(mediaCacheSize - (FILE_CACHE_THRESHOLD * MAX_MEDIA_PART)));
        }

        File thumbnailsCache = getThumbnailsCacheDirectory();
        long thumbnailsCacheSize = IoUtils.dirSize(thumbnailsCache);

        if (thumbnailsCacheSize > FILE_CACHE_THRESHOLD * MAX_THUMBNAILS_PART) {
            released += freeSpace(getFilesListToDelete(thumbnailsCache), Math.round(thumbnailsCacheSize - (FILE_CACHE_THRESHOLD * MAX_THUMBNAILS_PART)));
        }

        Timber.d("Released %d bytes from cache", released);

    }

    private File getExternalCachePath() {
        if (extStorageReady()) {
            // Retrieve the base path for the application in the external storage
            File externalStorageDir = Environment.getExternalStorageDirectory();

            // {SD_PATH}/Android/data/package_name/cache
            File extStorageAppCachePath = new File(externalStorageDir, "Android" + File.separator + "data" + File.separator + mPackageName + File.separator + CACHE_DIR_NAME);

            if (!extStorageAppCachePath.exists()) {
                Timber.d("External cache directory is missing. Creating one...");

                boolean success = extStorageAppCachePath.mkdirs();
                if (!success) {
                    Timber.e("Failed to create external cache directory");
                    return null;
                }
            }
            return extStorageAppCachePath;
        }
        return null;
    }

    private boolean extStorageReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private long freeSpace(List<File> files, long bytesToRelease) {
        long released = 0;

        if (files.isEmpty()) {
            return released;
        }

        for (File file : files) {
            long fileLength = file.length();
            String fileName = file.getAbsolutePath();
            long lastModified = file.lastModified();

            boolean isDeleted = file.delete();
            if (!isDeleted) {
                Timber.e("Error deleting file: %s", fileName);
                continue;
            }
            Timber.d("Deleted file: %s, size %s, last modified %s", fileName, fileLength, lastModified);
            released += fileLength;

            Timber.d("To release: %d", (bytesToRelease - released));
            if (released > bytesToRelease) {
                break;
            }
        }
        return released;
    }

    private List<File> getFilesListToDelete(File directoryPath) {
        List<File> filesList = new ArrayList<>();

        if (directoryPath != null && directoryPath.exists()) {
            File[] files = directoryPath.listFiles();
            if (files == null || files.length == 0) {
                return filesList;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    filesList.addAll(getFilesListToDelete(file));
                } else {
                    if (canDelete(file)) {
                        filesList.add(file);
                    }
                }

            }
        }
        //sort by date created
        Collections.sort(filesList, (f1, f2) -> {
            if (f1.lastModified() > f2.lastModified()) {
                return 1;
            } else if (f1.lastModified() < f2.lastModified()) {
                return -1;
            }
            return 0;
        });
        return filesList;
    }

    private boolean canDelete(File f) {
        //Check if it is cached page
        if (f.getParentFile().equals(getPagesCacheDirectory())) {
            //Check if it is old enough to be deleted
            if (f.lastModified() > System.currentTimeMillis() - mSettings.getCachePagesThresholdSize()/*days*/ * 24 * 60 * 60 * 1000) {
                return false;
            }
        }
        return true;
    }

}
