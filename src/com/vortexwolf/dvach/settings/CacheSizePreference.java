package com.vortexwolf.dvach.settings;

import java.io.File;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheManager;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;

public class CacheSizePreference extends Preference {
    private final CalculateCacheSizeTask mCalculateCacheSizeTask;
	private final File mExternalCacheDir;
	private final File mInternalCacheDir;
	
	public CacheSizePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		final Activity activity = (Activity)context;
		final MainApplication app = (MainApplication)activity.getApplication();
		ICacheManager cacheManager = app.getCacheManager();
		mExternalCacheDir = cacheManager.getExternalCacheDir();
		mInternalCacheDir = cacheManager.getInternalCacheDir();
		
		mCalculateCacheSizeTask = new CalculateCacheSizeTask();
		mCalculateCacheSizeTask.execute();
	}
	
	@Override
	protected void onPrepareForRemoval() {
		mCalculateCacheSizeTask.cancel(true);
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

	private class CalculateCacheSizeTask extends AsyncTask<Void, Long, Double>{

		@Override
		protected void onPreExecute() {
			setSummary(getContext().getString(R.string.loading));
		}
		
		@Override
		protected void onPostExecute(Double result) {
			String summary = result + " MB";
			setSummary(summary);
		}
		
		@Override
		protected Double doInBackground(Void... arg0) {
			double cacheSize = IoUtils.getSizeInMegabytes(mExternalCacheDir, mInternalCacheDir);
			
			return cacheSize;
		}
	}
}
