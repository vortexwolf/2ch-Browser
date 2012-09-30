package com.vortexwolf.dvach.services.domain;

import java.io.File;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;

public class DownloadFileService implements IDownloadFileService {
	
	public static final String TAG = "DownloadFileService";
	private final Resources mResources;
	
	public static boolean sNewClassAvailable;

   /* class initialization fails when this throws an exception */
   static {
	   if (Integer.valueOf(Build.VERSION.SDK) >= 9) {
           sNewClassAvailable = true;
       }
   }
	
	public DownloadFileService(Resources resources){
		this.mResources = resources;
	}
	
	@Override
	public void downloadFile(Context context, String uri){
		downloadFile(context, uri, null);
	}
	
	@Override
	public void downloadFile(Context context, String uri, File cachedFile){
		File to = this.getSaveFilePath(uri);
	    if(to.exists()){
	    	AppearanceUtils.showToastMessage(context, this.mResources.getString(R.string.error_file_exist));
	    	return;
	    }
	    
	    // В версиях до 2.3 копируем файл из кэша, если возможно
	    // В версиях начиная с 2.3 Download Manager всегда будет загружать файл заново, т.к. там более удобный интерфейс
	    Uri from = Uri.parse(uri);
		
	    boolean isCached = cachedFile != null && cachedFile.exists();
	    if(isCached){
	    	from = Uri.fromFile(cachedFile);
	    	new DownloadFileTask(context, from, to).execute();
	    }
	    else if (sNewClassAvailable) {
	        DownloadManagerWrapper.downloadFile(context, from, to);
		} 
	    else {
			new DownloadFileTask(context, from, to).execute();
		}
	}
	
	private File getSaveFilePath(String uri){
		Uri img = Uri.parse(uri);
	    String fileName = img.getLastPathSegment();
	    
	    File dir = new File(Environment.getExternalStorageDirectory() + "/download/2ch Browser/");
	    dir.mkdirs();
	    File file = new File(dir, fileName);
	    
	    return file;
	}
}
