package com.vortexwolf.dvach.settings;

import java.io.File;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.IoUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.preference.Preference;
import android.util.AttributeSet;

public class CacheSizePreference extends Preference {
	
	private File mExternalCacheDir;
	private File mInternalCacheDir;
	
	public CacheSizePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		MainApplication app = (MainApplication)((Activity)context).getApplication();
		mExternalCacheDir = app.getExternalCacheDir();
		mInternalCacheDir = app.getInternalCacheDir();
	}

	@Override
	public CharSequence getSummary() {
		long externalSize = mExternalCacheDir != null ? IoUtils.dirSize(mExternalCacheDir) : 0;
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
