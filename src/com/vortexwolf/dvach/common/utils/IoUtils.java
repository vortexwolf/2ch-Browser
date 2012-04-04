package com.vortexwolf.dvach.common.utils;

import java.io.File;

import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.app.Application;
import android.os.Environment;

public class IoUtils {

	public static long dirSize(File dir) {
		if(dir == null) return 0;
		
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
	
	public static void deleteDirectory(File path) {
		if (path != null && path.exists()) {
			File[] files = path.listFiles();
			if (files == null) {
				return;
			}
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}

			path.delete();
		}
	}
}
