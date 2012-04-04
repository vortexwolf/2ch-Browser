package com.vortexwolf.dvach.presentation.services;

import java.io.File;

import android.httpimage.FileSystemPersistence;
import android.os.Environment;

import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryChangedListener;
import com.vortexwolf.dvach.settings.ApplicationSettings;
import com.vortexwolf.dvach.settings.ICacheSettingsChangedListener;

public class CacheManager implements ICacheManager {
	
	private final String mPackageName;
	private final File mInternalCacheDir;
	private final File mExternalCacheDir;
	private final ApplicationSettings mSettings;
	
	public CacheManager(File internalCacheDir, String packageName, ApplicationSettings settings){
		this.mPackageName = packageName;
		this.mInternalCacheDir = internalCacheDir;
		this.mSettings = settings;
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
		if(mExternalCacheDir != null && mSettings.isFileCacheEnabled() && mSettings.isFileCacheSdCard()){
			return mExternalCacheDir;
		}
		
		return mInternalCacheDir;
	}
	
	public boolean isCacheEnabled(){
		return mSettings.isFileCacheEnabled();
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
