package com.vortexwolf.dvach.presentation.services;

import java.io.File;

import android.httpimage.FileSystemPersistence;
import android.os.Environment;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryChangedListener;
import com.vortexwolf.dvach.interfaces.ICacheManager;
import com.vortexwolf.dvach.interfaces.ICacheSettingsChangedListener;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class CacheManager implements ICacheManager {
	private static final String TAG = "CacheManager";
	
	private final String mPackageName;
	private final File mInternalCacheDir;
	private final File mExternalCacheDir;
	private final ApplicationSettings mSettings;
	private final Tracker mTracker;
	
	public CacheManager(File internalCacheDir, String packageName, ApplicationSettings settings, Tracker tracker){
		this.mPackageName = packageName;
		this.mInternalCacheDir = internalCacheDir;
		this.mSettings = settings;
		this.mTracker = tracker;
		this.mExternalCacheDir = getExternalCachePath();
	}
	
	@Override
	public File getInternalCacheDir() {
		return mInternalCacheDir;
	}
	
	@Override
	public File getExternalCacheDir() {
		return mExternalCacheDir;
	}
	
	@Override
	public File getCurrentCacheDirectory() {
		if(mExternalCacheDir != null && mSettings.isFileCacheEnabled() && mSettings.isFileCacheSdCard() && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			return mExternalCacheDir;
		}
		
		return mInternalCacheDir;
	}
	
	@Override
	public File getThumbnailsCacheDirectory(){
		return new File(this.getCurrentCacheDirectory(), "thumbnails");
	}
	
	@Override
	public File getPagesCacheDirectory(){
		return new File(this.getCurrentCacheDirectory(), "pages");
	}
	
	public void clearExcessCache(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				double cacheSize = IoUtils.getSizeInMegabytes(getExternalCacheDir(), getInternalCacheDir());

				mTracker.trackEvent(Tracker.CATEGORY_STATE, Tracker.ACTION_CACHE_FOLDER, getCurrentCacheDirectory().getAbsolutePath());
				mTracker.trackEvent(Tracker.CATEGORY_STATE, Tracker.ACTION_CACHE_SIZE, (int)(cacheSize * 1000));
				
				if(cacheSize > Constants.MAX_FILE_CACHE_SIZE){
					try{
						IoUtils.deleteDirectory(getReversedCacheDirectory()); //удаляем полностью противоположную кэшу директорию
						IoUtils.freeSpace(getCurrentCacheDirectory(), IoUtils.convertMbToBytes(cacheSize - Constants.MAX_FILE_CACHE_SIZE)); //удаляем лишний объем
					}
					catch(Exception e){
						MyLog.e(TAG, e);
					}
				}
			}
		})
		.start();
	}
	
	public boolean isCacheEnabled(){
		return mSettings.isFileCacheEnabled();
	}
	
	private File getReversedCacheDirectory(){
		return getCurrentCacheDirectory().equals(mExternalCacheDir) ? mInternalCacheDir : mExternalCacheDir;
	}
	
	private File getExternalCachePath() {
		// Check if the external storage is writeable
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// Retrieve the base path for the application in the external storage
			File externalStorageDir = Environment.getExternalStorageDirectory();
			// {SD_PATH}/Android/data/com.vortexwolf.dvach/cache
			File extStorageAppCachePath = new File(externalStorageDir,
					"Android" + File.separator + "data" + File.separator + mPackageName + File.separator + "cache");

			if (!extStorageAppCachePath.exists()) {
				extStorageAppCachePath.mkdirs();
			}

			return extStorageAppCachePath;
		}

		return null;
	}
}
