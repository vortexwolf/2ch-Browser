package com.vortexwolf.dvach.services;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.vortexwolf.dvach.common.Constants;

public class MyTracker {

    private final Tracker mTracker;

    public MyTracker(Context context) {
        GoogleAnalytics instance = GoogleAnalytics.getInstance(context);
        this.mTracker = instance.getTracker(Constants.ANALYTICS_KEY);

        instance.setDefaultTracker(this.mTracker);
        instance.setDebug(Constants.DEBUG);
    }

    public void startSession(Context context) {
    }

    public void stopSession() {
    }

    public Tracker getInnerTracker() {
        return this.mTracker;
    }

    // tracking methods
    public void trackActivityView(String pagePath) {
        this.mTracker.sendView(pagePath);
    }

    public void setBoardVar(String boardName) {
        this.mTracker.setCustomDimension(1, boardName);
    }

    public void clearBoardVar() {
        this.mTracker.setCustomDimension(1, "");
    }

    public void setPageNumberVar(int pageNumber) {
        this.mTracker.setCustomDimension(2, String.valueOf(pageNumber));
    }
}
