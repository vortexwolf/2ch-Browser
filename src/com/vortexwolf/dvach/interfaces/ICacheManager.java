package com.vortexwolf.dvach.interfaces;

import java.io.File;

public interface ICacheManager {

	public File getInternalCacheDir();

	public File getExternalCacheDir();

	public File getCurrentCacheDirectory();

	public abstract File getPagesCacheDirectory();

	public abstract File getThumbnailsCacheDirectory();

}