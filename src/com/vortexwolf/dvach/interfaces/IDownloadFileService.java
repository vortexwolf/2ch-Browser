package com.vortexwolf.dvach.interfaces;

import java.io.File;

import android.content.Context;

public interface IDownloadFileService {

	File getSaveFilePath(String uri);
	
	void downloadFile(Context context, String uri);

	public abstract void downloadFile(Context context, String uri, File cachedFile);
	
}