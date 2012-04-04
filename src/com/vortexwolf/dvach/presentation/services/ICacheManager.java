package com.vortexwolf.dvach.presentation.services;

import java.io.File;

public interface ICacheManager {

	public File getInternalCacheDir();

	public File getExternalCacheDir();

	public File getCurrentCacheDirectory();

}