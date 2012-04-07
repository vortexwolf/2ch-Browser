package com.vortexwolf.dvach.settings;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheManager;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class CacheSizePreference extends Preference {
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(1);
	private final File mExternalCacheDir;
	private final File mInternalCacheDir;
	
	public CacheSizePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		MainApplication app = (MainApplication)((Activity)context).getApplication();
		ICacheManager cacheManager = app.getCacheManager();
		mExternalCacheDir = cacheManager.getExternalCacheDir();
		mInternalCacheDir = cacheManager.getInternalCacheDir();
		
		this.setSummary(context.getString(R.string.loading));
		
		mExecutor.execute(new Runnable(){
			@Override
			public void run() {
				try{
					double cacheSize = IoUtils.getSizeInMegabytes(mExternalCacheDir, mInternalCacheDir);
	
					String summary = cacheSize + " MB";
					if(!mExecutor.isTerminated()){
						CacheSizePreference.this.setSummary(summary);
					}
				}
				catch(Exception e){
					MyLog.e("CacheSizePreference", e);
				}
			}
		});		
	}
	
	@Override
	protected void onPrepareForRemoval() {
		mExecutor.shutdownNow();
		super.onPrepareForRemoval();
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
