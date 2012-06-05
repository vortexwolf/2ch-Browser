package com.vortexwolf.dvach.services;

import java.io.File;

import android.os.Environment;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class CacheDirectoryManager implements ICacheDirectoryManager {
	private static final String TAG = "CacheManager";
	
	private final String mPackageName;
	private final File mInternalCacheDir;
	private final File mExternalCacheDir;
	private final ApplicationSettings mSettings;
	private final Tracker mTracker;
	
	public CacheDirectoryManager(File internalCacheDir, String packageName, ApplicationSettings settings, Tracker tracker){
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
		File currentDirectory;
		
		if(mExternalCacheDir != null && mSettings.isFileCacheEnabled() && mSettings.isFileCacheSdCard() && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			currentDirectory = mExternalCacheDir;
		}
		else{
			currentDirectory = mInternalCacheDir;
		}
		
		if(!currentDirectory.exists()){
			currentDirectory.mkdirs();
		}
		
		return currentDirectory;
	}
	
	@Override
	public File getThumbnailsCacheDirectory(){
		return new File(this.getCurrentCacheDirectory(), "thumbnails");
	}
	
	@Override
	public File getPagesCacheDirectory(){
		return new File(this.getCurrentCacheDirectory(), "pages");
	}
	
	@Override
	public void trimCacheIfNeeded(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				long cacheSize = IoUtils.dirSize(getCurrentCacheDirectory());
				long maxSize = Constants.FILE_CACHE_THRESHOLD;
				
				if(cacheSize > maxSize){
					long deleteAmount = (cacheSize - maxSize) + Constants.FILE_CACHE_TRIM_AMOUNT;
					IoUtils.deleteDirectory(getReversedCacheDirectory()); //удаляем полностью противоположную кэшу директорию
					IoUtils.freeSpace(getCurrentCacheDirectory(), deleteAmount); //удаляем лишний объем
				}
			}
		})
		.start();
	}
	
	@Override
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

			return extStorageAppCachePath;
		}

		return null;
	}
}
