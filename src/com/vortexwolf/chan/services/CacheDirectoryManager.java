package com.vortexwolf.chan.services;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.settings.ApplicationSettings;

import java.io.File;

public class CacheDirectoryManager {
    private static final String TAG = CacheDirectoryManager.class.getSimpleName();

    public long FILE_CACHE_THRESHOLD;

    private final String mPackageName;
    private final File mInternalCacheDir;
    private final File mExternalCacheDir;
    private final ApplicationSettings mSettings;
    private TrimCache trimCache;

    public CacheDirectoryManager(File internalCacheDir, String packageName, ApplicationSettings settings) {
        this.mPackageName = packageName;
        this.mInternalCacheDir = internalCacheDir;
        this.mSettings = settings;
        this.mExternalCacheDir = this.getExternalCachePath();
        this.FILE_CACHE_THRESHOLD = IoUtils.convertMbToBytes(mSettings.getCacheSize());
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

        if (!currentDirectory.exists()) {
            currentDirectory.mkdirs();
        }

        return currentDirectory;
    }

    public File getThumbnailsCacheDirectory() {
        return this.getCacheDirectory("thumbnails");
    }

    public File getPagesCacheDirectory() {
        return this.getCacheDirectory("pages");
    }

    public File getImagesCacheDirectory() {
        return this.getCacheDirectory("images");
    }

    public File getWebmCacheDirectory() {
        return this.getCacheDirectory("webm");
    }

    private File getCacheDirectory(String subFolder) {
        File file = new File(this.getCurrentCacheDirectory(), subFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    public File getCachedImageFileForWrite(Uri uri) {
        String fileName = uri.getLastPathSegment();
        boolean isWebm = UriUtils.isWebmUri(uri);

        File cachedFile = new File(isWebm ? this.getWebmCacheDirectory() : this.getImagesCacheDirectory(), fileName);

        return cachedFile;
    }

    public File getCachedImageFileForRead(Uri uri) {
        File cachedFile = this.getCachedImageFileForWrite(uri);
        if (!cachedFile.exists()) {
            cachedFile = IoUtils.getSaveFilePath(uri, this.mSettings);
        }

        return cachedFile;
    }

    public void trimCacheIfNeeded() {
        long cacheSize = IoUtils.dirSize(CacheDirectoryManager.this.getCurrentCacheDirectory());
        long maxSize = FILE_CACHE_THRESHOLD;

        if (cacheSize > maxSize) {
            if (trimCache == null) {
                trimCache = new TrimCache();
                trimCache.execute();
            } else {
                switch (trimCache.getStatus()) {
                    case PENDING:
                    case RUNNING:
                        break;
                    case FINISHED:
                        trimCache = new TrimCache();
                        trimCache.execute();
                        break;

                }
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
            if (!extStorageAppCachePath.mkdirs()) {
                return null;
            }

            return extStorageAppCachePath;
        }

        return null;
    }

    private class TrimCache extends AsyncTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... params) {
            Log.d(TAG, "Trimming cache");

            //Delete cache directory that might be previously used by user.
            IoUtils.deleteDirectory(CacheDirectoryManager.this.getReversedCacheDirectory());

            //Trim current cache directory

            //40% of cache space is reserved for webm. Clear it by half if overflowed.
            if (IoUtils.dirSize(CacheDirectoryManager.this.getWebmCacheDirectory()) > FILE_CACHE_THRESHOLD * 0.4) {
                IoUtils.freeSpace(CacheDirectoryManager.this.getWebmCacheDirectory(),
                        Math.round(FILE_CACHE_THRESHOLD * 0.4 * 0.5));
            }

            //15% of cache space is reserved for pages. Clear it by 20% if overflowed.
            if (IoUtils.dirSize(CacheDirectoryManager.this.getPagesCacheDirectory()) > FILE_CACHE_THRESHOLD * 0.15) {
                IoUtils.freeSpace(CacheDirectoryManager.this.getPagesCacheDirectory(),
                        Math.round(FILE_CACHE_THRESHOLD * 0.15 * 0.2));
            }

            //15% of cache space is reserved for images. Clear it by 20% if overflowed.
            if (IoUtils.dirSize(CacheDirectoryManager.this.getImagesCacheDirectory()) > FILE_CACHE_THRESHOLD * 0.15) {
                IoUtils.freeSpace(CacheDirectoryManager.this.getImagesCacheDirectory(),
                        Math.round(FILE_CACHE_THRESHOLD * 0.15 * 0.2));
            }

            //15% of cache space is reserved for thumbnails. Clear it by 20% if overflowed.
            if (IoUtils.dirSize(CacheDirectoryManager.this.getThumbnailsCacheDirectory()) > FILE_CACHE_THRESHOLD * 0.15) {
                IoUtils.freeSpace(CacheDirectoryManager.this.getThumbnailsCacheDirectory(),
                        Math.round(FILE_CACHE_THRESHOLD * 0.15 * 0.2));
            }
            return null;
        }

    }

}
