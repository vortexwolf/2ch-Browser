package com.vortexwolf.dvach.presentation.services;

import android.content.Context;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.vortexwolf.dvach.common.Constants;

public class Tracker {

	public static final String CATEGORY_UI = "UI";
	public static final String CATEGORY_PREFERENCES = "Preferences";
	public static final String CATEGORY_STATE = "Application State";
	
	public static final String ACTION_DOWNLOAD_FILE = "Download File";
	public static final String ACTION_EXTERNAL_BROWSER_OPTION = "Open External Browser";
	public static final String ACTION_SELECT_IMAGE_FROM_FILES = "Select Image From Files";
	public static final String ACTION_SELECT_IMAGE_FROM_GALLERY = "Select Image From Gallery";
	public static final String ACTION_CONTEXT_REPLY_POST = "Reply Post Context Menu";
	public static final String ACTION_CONTEXT_REPLY_POST_QUOTE = "Reply Post With Quote Context Menu";
	public static final String ACTION_CONTEXT_COPY_POST = "Copy Post Context Menu";
	
	public static final String ACTION_PREFERENCE_THEME = "Change Theme";
	public static final String ACTION_PREFERENCE_TEXT_SIZE = "Change Text Size";
	public static final String ACTION_PREFERENCE_HOME_PAGE = "Change Home Page";
	public static final String ACTION_PREFERENCE_LOAD_THUMBNAILS = "Change Load Thumbnails";
	public static final String ACTION_PREFERENCE_DISPLAY_DATE = "Change Display Date";
	public static final String ACTION_PREFERENCE_POPUP_LINK = "Change Popup Links";
	public static final String ACTION_PREFERENCE_AUTO_REFRESH = "Change Auto Refresh";
	public static final String ACTION_PREFERENCE_NAVIGATION_BAR = "Change Display Navigation Bar";
	public static final String ACTION_PREFERENCE_YOUTUBE_MOBILE = "Change Youtube mobile links";
	public static final String ACTION_PREFERENCE_FILE_CACHE = "Change File cache";
	public static final String ACTION_PREFERENCE_FILE_CACHE_SD = "Change File cache on sd card";
	
	public static final String LABEL_DOWNLOAD_FILE_FROM_BROWSER = "From browser";
	public static final String LABEL_DOWNLOAD_FILE_FROM_CONTEXT_MENU = "From context menu";
	
	private final GoogleAnalyticsTracker mTracker;

	public Tracker() {
		mTracker = GoogleAnalyticsTracker.getInstance();
	}
		
	public void startSession(Context context){
		this.mTracker.startNewSession(Constants.ANALYTICS_KEY, 120, context);
	}
	
	public void stopSession(){
		this.mTracker.stopSession();
	}

	public GoogleAnalyticsTracker getInnerTracker(){
		return this.mTracker;
	}
	
	// tracking methods
	public void trackActivityView(String pagePath) {
		mTracker.trackPageView(pagePath);
	}

	public void trackEvent(String category, String action) {
		mTracker.trackEvent(category, action, "", 0);
	}
	
	public void trackEvent(String category, String action, String label) {
		mTracker.trackEvent(category, action, label, 0);
	}
	
	public void trackEvent(String category, String action, int value) {
		mTracker.trackEvent(category, action, "", value);
	}
	
	public void trackEvent(String category, String action, String label, int value) {
		mTracker.trackEvent(category, action, label, value);
	}
	
	public void setBoardVar(String boardName){
		mTracker.setCustomVar(1, "Board Name", boardName);
	}
	
	public void clearBoardVar(){
		mTracker.setCustomVar(1, "Board Name", "");
	}
	
	public void setPageNumberVar(int pageNumber){
		mTracker.setCustomVar(2, "Page Number", String.valueOf(pageNumber));
	}
}
