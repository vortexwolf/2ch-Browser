package com.vortexwolf.dvach.interfaces;

import java.io.File;

public interface ICacheDirectoryManager {

	public File getInternalCacheDir();

	public File getExternalCacheDir();

	public File getCurrentCacheDirectory();

	public abstract File getPagesCacheDirectory();

	public abstract File getThumbnailsCacheDirectory();

	public abstract boolean isCacheEnabled();

	public abstract void trimCacheIfNeeded();

}