package com.vortexwolf.dvach.settings;

public interface ICacheSettingsChangedListener {
	void cacheFileSystemChanged(boolean newValue);
	void cacheSDCardChanged(boolean newValue);
}
