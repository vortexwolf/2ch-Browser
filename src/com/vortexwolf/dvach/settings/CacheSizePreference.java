package com.vortexwolf.dvach.settings;

import java.io.File;

import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.presentation.services.ICacheManager;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class CacheSizePreference extends Preference {
	
	private File mExternalCacheDir;
	private File mInternalCacheDir;
	
	public CacheSizePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		MainApplication app = (MainApplication)((Activity)context).getApplication();
		ICacheManager cacheManager = app.getCacheManager();
		mExternalCacheDir = cacheManager.getExternalCacheDir();
		mInternalCacheDir = cacheManager.getInternalCacheDir();
	}

	@Override
	public CharSequence getSummary() {
		long externalSize = IoUtils.dirSize(mExternalCacheDir);
		long internalSize = IoUtils.dirSize(mInternalCacheDir);
		
		double allSizeMb = (externalSize + internalSize)/ 1024d / 1024d;
		return (allSizeMb > 0 ? Math.round(allSizeMb * 100) / 100d : 0) + " MB";
	}

	@Override
	protected void onClick() {
		if(mExternalCacheDir != null){
			IoUtils.deleteDirectory(mExternalCacheDir);
			mExternalCacheDir.mkdir();
		}
		
		IoUtils.deleteDirectory(mInternalCacheDir);
		mInternalCacheDir.mkdir();
		
		this.setSummary(this.getSummary());
		
		super.onClick();
	}

}
