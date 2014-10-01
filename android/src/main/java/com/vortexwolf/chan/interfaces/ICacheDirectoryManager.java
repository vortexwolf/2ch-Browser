package com.vortexwolf.chan.interfaces;

import java.io.File;

import android.net.Uri;

public interface ICacheDirectoryManager {

    public File getInternalCacheDir();

    public File getExternalCacheDir();

    public File getCurrentCacheDirectory();

    public abstract File getPagesCacheDirectory();

    public abstract File getThumbnailsCacheDirectory();

    public abstract void trimCacheIfNeeded();

    public abstract File getImagesCacheDirectory();

    public abstract File getCachedImageFileForWrite(Uri uri);

    public abstract File getCachedImageFileForRead(Uri uri);

}