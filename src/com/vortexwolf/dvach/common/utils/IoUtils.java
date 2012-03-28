package com.vortexwolf.dvach.common.utils;

import java.io.File;

import android.app.Application;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Environment;

public class IoUtils {

	public static long dirSize(File dir) {
	    long result = 0;
	    File[] fileList = dir.listFiles();

	    for(int i = 0; i < fileList.length; i++) {
	        // Recursive call if it's a directory
	        if(fileList[i].isDirectory()) {
	            result += dirSize(fileList [i]);
	        } else {
	            // Sum the file size in bytes
	            result += fileList[i].length();
	        }
	    }
	    return result; // return the file size
	}
	
	public static boolean deleteDirectory(File path) {
	    if(path.exists()) {
	      File[] files = path.listFiles();
	      if (files == null) {
	          return true;
	      }
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return(path.delete());
	}
	
	public static File tryGetExternalCachePath(Application app){
		// Check if the external storage is writeable
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			// Retrieve the base path for the application in the external storage
			File externalStorageDir = Environment.getExternalStorageDirectory();
			// {SD_PATH}/Android/data/com.vortexwolf.dvach/cache
			File extStorageAppCachePath = new File(externalStorageDir,
					"Android" + File.separator + "data" + File.separator + app.getPackageName() + File.separator + "cache");

			if (!extStorageAppCachePath.exists())
			{
				extStorageAppCachePath.mkdirs();
			}
			
			return extStorageAppCachePath;
		}
		
		return null;
	}

}
