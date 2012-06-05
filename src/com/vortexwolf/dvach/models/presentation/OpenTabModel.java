package com.vortexwolf.dvach.models.presentation;

import android.net.Uri;

import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;

public class OpenTabModel {

	private final String mTitle;
	private final Uri mUri;
	
	private AppearanceUtils.ListViewPosition mPosition;
	
	public OpenTabModel(String title, String boardName, int pageNumber){
		this.mTitle = title;
		this.mUri = Uri.parse(UriUtils.create2chURL(boardName, pageNumber));
	}
	
	public OpenTabModel(String title, String boardName, String threadNumber){
		this.mTitle = title;
		this.mUri = Uri.parse(UriUtils.create2chThreadURL(boardName, threadNumber));
	}

	public String getTitle() {
		return mTitle;
	}

	public Uri getUri() {
		return mUri;
	}

	public void setPosition(AppearanceUtils.ListViewPosition position) {
		this.mPosition = position;
	}

	public AppearanceUtils.ListViewPosition getPosition() {
		return mPosition;
	}
}
