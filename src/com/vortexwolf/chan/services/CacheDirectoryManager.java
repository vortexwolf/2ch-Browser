package com.vortexwolf.chan.services;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.settings.ApplicationSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CacheDirectoryManager implements Runnable {
    private static final String LOG_TAG = CacheDirectoryManager.class.getSimpleName();

    private final String mPackageName;
    private final File mInternalCacheDir;
    private final File mExternalCacheDir;
    private final ApplicationSettings mSettings;
    private TrimCache trimCache;


    public CacheDirectoryManager(File internalCacheDir, String packageName, ApplicationSettings settings) {
        this.mPackageName = packageName;
        this.mInternalCacheDir = new File(internalCacheDir, "cache");
        this.mSettings = settings;
        this.mExternalCacheDir = this.getExternalCachePath();
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            trimCacheIfNeeded();
        }
    }

    public long getCacheSize() {
        return mSettings.getCacheSize();
    }

    public File getInternalCacheDir() {
        return this.mInternalCacheDir;
    }

    public File getExternalCacheDir() {
        return this.mExternalCacheDir;
    }

    public File getCurrentCacheDirectory() {
        File currentDirectory;

        if (this.mExternalCacheDir != null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            currentDirectory = this.mExternalCacheDir;
        } else {
            currentDirectory = this.mInternalCacheDir;
        }

        return currentDirectory;
    }

    public File getThumbnailsCacheDirectory() {
        return this.getCacheDirectory("thumbnails");
    }

    public File getPagesCacheDirectory() {
        return this.getCacheDirectory("pages");
    }

    public File getMediaCacheDirectory() {
        return this.getCacheDirectory("images");
    }

    private File getCacheDirectory(String subFolder) {
        File file = new File(this.getCurrentCacheDirectory(), subFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    public File getCachedMediaFileForWrite(Uri uri) {
        String fileName = uri.getLastPathSegment();

        return new File(this.getMediaCacheDirectory(), fileName);
    }

    public File getCachedMediaFileForRead(Uri uri) {
        File cachedFile = this.getCachedMediaFileForWrite(uri);
        if (!cachedFile.exists()) {
            cachedFile = IoUtils.getSaveFilePath(uri, this.mSettings);
        }

        return cachedFile;
    }

    public void trimCacheIfNeeded() {
        long cacheSize = IoUtils.dirSize(getCurrentCacheDirectory());
        long maxSize = IoUtils.convertMbToBytes(mSettings.getCacheSize());

        if (cacheSize > maxSize) {
            if (trimCache == null) {
                trimCache = new TrimCache(mSettings);
                trimCache.execute();
            } else {
                switch (trimCache.getStatus()) {
                    case PENDING:
                    case RUNNING:
                        MyLog.d(LOG_TAG, "Cache trimming in progress, sleeping for next 60 seconds");
                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case FINISHED:
                        trimCache = new TrimCache(mSettings);
                        trimCache.execute();
                        break;

                }
            }
        } else {
            MyLog.d(LOG_TAG, "Cache trimming is not required, sleeping for next 60 seconds");
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private File getReversedCacheDirectory() {
        return this.getCurrentCacheDirectory().equals(this.mExternalCacheDir)
                ? this.mInternalCacheDir
                : this.mExternalCacheDir;
    }

    private File getExternalCachePath() {
        // Check if the external storage is writable
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // Retrieve the base path for the application in the external
            // storage
            File externalStorageDir = Environment.getExternalStorageDirectory();
            // {SD_PATH}/Android/data/com.vortexwolf.chan/cache
            File extStorageAppCachePath = new File(externalStorageDir, "Android" + File.separator + "data" + File.separator + this.mPackageName + File.separator + "cache");
            extStorageAppCachePath.mkdirs();

            return extStorageAppCachePath;
        }

        return null;
    }

    private class TrimCache extends AsyncTask<Void, Void, Long> {
        private final long FILE_CACHE_THRESHOLD;
        private final float MAX_THUMBNAILS_PART;
        private final float MAX_MEDIA_PART;
        private final float MAX_PAGES_PART;


        TrimCache(final ApplicationSettings mSettings) {
            FILE_CACHE_THRESHOLD = Math.round(IoUtils.convertMbToBytes(mSettings.getCacheSize()));
            MAX_THUMBNAILS_PART = mSettings.getCacheThumbnailsSize() / 100f;
            MAX_MEDIA_PART = mSettings.getCacheMediaSize() / 100f;
            MAX_PAGES_PART = mSettings.getCachePagesSize() / 100f;
        }

        @Override
        public Long doInBackground(Void... params) {
            MyLog.d(LOG_TAG, "Trimming cache");

            //Delete cache directory that might be previously used by user.
            IoUtils.deleteDirectory(getReversedCacheDirectory());

            long released = 0;

            //Trim current cache directory

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
            return released;
        }

    }

    private static long freeSpace(List<File> files, long bytesToRelease) {
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
                MyLog.e(LOG_TAG, "Error deleting file: " + fileName);
                continue;
            }
            MyLog.d(LOG_TAG, "Deleted file: " + fileName + ", file size: " + fileLength + ", last modified: " + lastModified);
            released += fileLength;
            MyLog.d(LOG_TAG, "To release: " + (bytesToRelease - released));
            if (released > bytesToRelease) {
                break;
            }
        }

        return released;
    }

    private List<File> getFilesListToDelete(File directoryPath) {
        List<File> files_list = new ArrayList<>();

        if (directoryPath != null && directoryPath.exists()) {
            File[] files = directoryPath.listFiles();
            if (files == null || files.length == 0) {
                return files_list;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    files_list.addAll(getFilesListToDelete(file));
                } else {
                    if (canDelete(file)) {
                        files_list.add(file);
                    }
                }

            }
        }
        //sort by date created
        Collections.sort(files_list, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.lastModified() > f2.lastModified()) {
                    return 1;
                } else if (f1.lastModified() < f2.lastModified()) {
                    return -1;
                }
                return 0;
            }
        });
        return files_list;
    }

    private boolean canDelete(File f) {
        //Check if it is cached page
        if (f.getParentFile().equals(getPagesCacheDirectory())) {
            //Check if it is old enough to be deleted
            if (f.lastModified() > System.currentTimeMillis() - mSettings.getCachePagesThresholdSize() * 24 * 60 * 60 * 1000) {
                return false;
            }
        }
        return true;
    }

}
