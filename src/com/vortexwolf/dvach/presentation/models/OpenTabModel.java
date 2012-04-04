package com.vortexwolf.dvach.presentation.models;

import android.net.Uri;

import com.vortexwolf.dvach.common.utils.UriUtils;

public class OpenTabModel {

	private final OpenTabType mTabType;
	private final String mTitle;
	private final Uri mUri;
	
	public OpenTabModel(String title, String boardName, int pageNumber){
		this.mTitle = title;
		this.mTabType = OpenTabType.BOARD;
		this.mUri = Uri.parse(UriUtils.create2chURL(boardName, pageNumber));
	}
	
	public OpenTabModel(String title, String boardName, String threadNumber){
		this.mTitle = title;
		this.mTabType = OpenTabType.THREAD;
		this.mUri = Uri.parse(UriUtils.create2chThreadURL(boardName, threadNumber));
	}

	public String getTitle() {
		return mTitle;
	}

	public Uri getUri() {
		return mUri;
	}

	public OpenTabType getTabType() {
		return mTabType;
	}
}
