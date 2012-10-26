package com.vortexwolf.dvach.models.presentation;

import android.net.Uri;

import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;

public class OpenTabModel {

	private final String mTitle;
	private final Uri mUri;
	
	private AppearanceUtils.ListViewPosition mPosition;
	
	public OpenTabModel(String title, Uri uri){
		this.mTitle = title;
		this.mUri = uri;
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
