package com.vortexwolf.chan.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.settings.ApplicationSettings;

import java.io.File;
import java.io.IOException;

public class CacheDirectoryManager {
    private static final String TAG = "CacheManager";

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final String mPackageName;
    private final File mInternalCacheDir;
    private final File mExternalCacheDir;
    private final ApplicationSettings mSettings;

    public CacheDirectoryManager(File internalCacheDir, String packageName, ApplicationSettings settings) {
        this.mPackageName = packageName;
        this.mInternalCacheDir = internalCacheDir;
        this.mSettings = settings;
        this.mExternalCacheDir = this.getExternalCachePath();
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

    private File getCacheDirectory(String subFolder) {
        File file = new File(this.getCurrentCacheDirectory(), subFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    public File getCachedImageFileForWrite(Uri uri) {
        String fileName = uri.getLastPathSegment();

        File cachedFile = new File(this.getImagesCacheDirectory(), fileName);

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
            if(!extStorageAppCachePath.mkdirs()){
                return null;
            };

            return extStorageAppCachePath;
        }

        return null;
    }

    public static void verifyStoragePermissions(Activity activity) {
        //TODO rework permissions gain logic
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
