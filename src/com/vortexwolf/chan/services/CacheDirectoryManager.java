package com.vortexwolf.chan.services;

import java.io.File;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.interfaces.ICacheDirectoryManager;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class CacheDirectoryManager implements ICacheDirectoryManager {
    private static final String TAG = "CacheManager";

    private final String mPackageName;
    private final File mInternalCacheDir;
    private final File mExternalCacheDir;
    private final ApplicationSettings mSettings;
    private final MyTracker mTracker;

    public CacheDirectoryManager(File internalCacheDir, String packageName, ApplicationSettings settings, MyTracker tracker) {
        this.mPackageName = packageName;
        this.mInternalCacheDir = internalCacheDir;
        this.mSettings = settings;
        this.mTracker = tracker;
        this.mExternalCacheDir = this.getExternalCachePath();
    }

    @Override
    public File getInternalCacheDir() {
        return this.mInternalCacheDir;
    }

    @Override
    public File getExternalCacheDir() {
        return this.mExternalCacheDir;
    }

    @Override
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

    @Override
    public File getThumbnailsCacheDirectory() {
        return this.getCacheDirectory("thumbnails");
    }

    @Override
    public File getPagesCacheDirectory() {
        return this.getCacheDirectory("pages");
    }

    @Override
    public File getImagesCacheDirectory() {
        return this.getCacheDirectory("images");
    }

    private File getCacheDirectory(String subFolder) {
        File file = new File(this.getCurrentCacheDirectory(), subFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    @Override
    public File getCachedImageFileForWrite(Uri uri) {
        String fileName = uri.getLastPathSegment();

        File cachedFile = new File(this.getImagesCacheDirectory(), fileName);

        return cachedFile;
    }

    @Override
    public File getCachedImageFileForRead(Uri uri) {
        File cachedFile = this.getCachedImageFileForWrite(uri);
        if (!cachedFile.exists()) {
            cachedFile = IoUtils.getSaveFilePath(uri, this.mSettings);
        }

        return cachedFile;
    }

    @Override
    public void trimCacheIfNeeded() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... params) {
                long cacheSize = IoUtils.dirSize(CacheDirectoryManager.this.getCurrentCacheDirectory());
                long maxSize = Constants.FILE_CACHE_THRESHOLD;

                if (cacheSize > maxSize) {
                    long deleteAmount = (cacheSize - maxSize) + Constants.FILE_CACHE_TRIM_AMOUNT;
                    IoUtils.deleteDirectory(CacheDirectoryManager.this.getReversedCacheDirectory());
                    deleteAmount -= IoUtils.freeSpace(CacheDirectoryManager.this.getImagesCacheDirectory(), deleteAmount); 
                    IoUtils.freeSpace(CacheDirectoryManager.this.getCurrentCacheDirectory(), deleteAmount);
                }
                
                return null;
            }
        };
        
        task.execute();
    }

    private File getReversedCacheDirectory() {
        return this.getCurrentCacheDirectory().equals(this.mExternalCacheDir)
                ? this.mInternalCacheDir
                : this.mExternalCacheDir;
    }

    private File getExternalCachePath() {
        // Check if the external storage is writeable
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // Retrieve the base path for the application in the external
            // storage
            File externalStorageDir = Environment.getExternalStorageDirectory();
            // {SD_PATH}/Android/data/com.vortexwolf.chan/cache
            File extStorageAppCachePath = new File(externalStorageDir, "Android" + File.separator + "data" + File.separator + this.mPackageName + File.separator + "cache");

            return extStorageAppCachePath;
        }

        return null;
    }
}
