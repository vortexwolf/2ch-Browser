package com.vortexwolf.dvach.interfaces;

import java.io.File;

import android.content.Context;

public interface IDownloadFileService {
	void downloadFile(Context context, String uri);

	public abstract void downloadFile(Context context, String uri, File cachedFile);
	
}