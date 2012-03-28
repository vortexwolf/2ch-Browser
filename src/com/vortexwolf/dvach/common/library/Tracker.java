package com.vortexwolf.dvach.common.library;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Tracker {

	public static final String CATEGORY_SEND = "Send";
	public static final String CATEGORY_UI = "UI";
	public static final String CATEGORY_PREFERENCES = "Preferences";
	
	public static final String ACTION_NEW_THREAD = "Create New Thread";
	public static final String ACTION_NEW_POST = "Add New Post";
	public static final String ACTION_ATTACH_FILE = "Attach File";
	
	public static final String ACTION_SELECT_BOARD = "Select Board";
	public static final String ACTION_DOWNLOAD_FILE = "Download File";
	public static final String ACTION_EXTERNAL_BROWSER_OPTION = "Open External Browser";
	public static final String ACTION_SELECT_IMAGE_FROM_GALLERY = "Select Image From Gallery";
	
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
	private static Tracker sInstance;
	
	private Tracker(GoogleAnalyticsTracker tracker) {
		mTracker = tracker;
	}
	
	public static Tracker getInstance(){
		if(sInstance == null){
			sInstance = new Tracker(GoogleAnalyticsTracker.getInstance());
		}
		
		return sInstance;
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
