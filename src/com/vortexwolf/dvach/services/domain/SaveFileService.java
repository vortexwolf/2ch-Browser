package com.vortexwolf.dvach.services.domain;

import java.io.File;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.asynctasks.DownloadFileTask;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.library.DialogProgressView;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;
import com.vortexwolf.dvach.interfaces.IProgressView;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class SaveFileService {
	
	public static final String TAG = "DownloadFileService";
	
	private final Resources mResources;
	private final ApplicationSettings mSettings;
	private final DownloadFileService mDownloadFileService;
	private final ICacheDirectoryManager mCacheDirectoryManager;
	
	public SaveFileService(Resources resources, ApplicationSettings settings, DownloadFileService downloadFileService, ICacheDirectoryManager cacheDirectoryManager){
		this.mResources = resources;
		this.mSettings = settings;
		this.mDownloadFileService = downloadFileService;
		this.mCacheDirectoryManager = cacheDirectoryManager;
	}
	
	public void downloadFile(Context context, String uri){
		this.downloadFile(context, uri, this.mSettings.isDownloadInBackground());
	}
	
	public void downloadFile(Context context, String uri, boolean isDownloadInBackground){
		File file = this.tryGetFileFromCache(Uri.parse(uri));
		
		this.downloadFile(context, Uri.parse(uri), file.exists() ? file : null, isDownloadInBackground);
	}
	
	private void downloadFile(Context context, Uri uri, File cachedFile, boolean isDownloadInBackground){
		File to = this.getSaveFilePath(uri);
	    if(to.exists()){
	    	AppearanceUtils.showToastMessage(context, this.mResources.getString(R.string.error_file_exist));
	    	return;
	    }
	    
	    Uri from = uri;
	    if(cachedFile != null && cachedFile.exists()){
	    	from = Uri.fromFile(cachedFile);
	    }	    

	    IProgressView progressView = isDownloadInBackground ? null : new DialogProgressView(context);
	    
    	new DownloadFileTask(context, from, to, this.mDownloadFileService, progressView).execute();
	}
	
	private File tryGetFileFromCache(Uri uri){
        String hashCode = String.format("%08x", uri.hashCode());
        File file = new File(this.mCacheDirectoryManager.getImagesCacheDirectory(), hashCode);
        
        return file;
	}
	
	private File getSaveFilePath(Uri uri){
	    String fileName = uri.getLastPathSegment();
	    
	    File dir = new File(Environment.getExternalStorageDirectory(), this.mSettings.getDownloadPath());
	    dir.mkdirs();
	    File file = new File(dir, fileName);
	    
	    return file;
	}
}
