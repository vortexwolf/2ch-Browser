package com.vortexwolf.dvach.common.http;

import java.io.File;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.vortexwolf.dvach.common.Errors;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;

public class DownloadFileService implements IDownloadFileService {
	
	public static final String TAG = "DownloadFileService";
	private final Errors mErrors;
	
	public static boolean sNewClassAvailable;

   /* class initialization fails when this throws an exception */
   static {
       try {
    	   DownloadManagerWrapper.checkAvailable();
           sNewClassAvailable = true;
       } catch (Throwable t) {
    	   sNewClassAvailable = false;
       }
   }
	
	public DownloadFileService(Errors errors){
		this.mErrors = errors;
	}
	
	@Override
	public File getSaveFilePath(String uri){
		Uri img = Uri.parse(uri);
	    String fileName = img.getLastPathSegment();
	    
	    File dir = new File(Environment.getExternalStorageDirectory() + "/download/2ch Browser/");
	    dir.mkdirs();
	    File file = new File(dir, fileName);
	    
	    return file;
	}
	
	@Override
	public void downloadFile(Context context, String uri){
		File file = this.getSaveFilePath(uri);
	    if(file.exists()){
	    	AppearanceUtils.showToastMessage(context, this.mErrors.getFileExistError());
	    	return;
	    }
	        
		if (sNewClassAvailable) {
	        DownloadManagerWrapper.downloadFile(context, Uri.parse(uri), file);
		} else {
			new DownloadFileTask(context, uri, file, mErrors).execute();
		}
	}
}
