package com.vortexwolf.dvach.services;

import android.content.Context;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.vortexwolf.dvach.common.Constants;

public class Tracker {

    private final GoogleAnalyticsTracker mTracker;

    public Tracker() {
        this.mTracker = GoogleAnalyticsTracker.getInstance();
    }

    public void startSession(Context context) {
        this.mTracker.startNewSession(Constants.ANALYTICS_KEY, 120, context);
    }

    public void stopSession() {
        this.mTracker.stopSession();
    }

    public GoogleAnalyticsTracker getInnerTracker() {
        return this.mTracker;
    }

    // tracking methods
    public void trackActivityView(String pagePath) {
        this.mTracker.trackPageView(pagePath);
    }

    public void trackEvent(String category, String action) {
        this.mTracker.trackEvent(category, action, "", 0);
    }

    public void trackEvent(String category, String action, String label) {
        this.mTracker.trackEvent(category, action, label, 0);
    }

    public void trackEvent(String category, String action, int value) {
        this.mTracker.trackEvent(category, action, "", value);
    }

    public void trackEvent(String category, String action, String label, int value) {
        this.mTracker.trackEvent(category, action, label, value);
    }

    public void setBoardVar(String boardName) {
        this.mTracker.setCustomVar(1, "Board Name", boardName);
    }

    public void clearBoardVar() {
        this.mTracker.setCustomVar(1, "Board Name", "");
    }

    public void setPageNumberVar(int pageNumber) {
        this.mTracker.setCustomVar(2, "Page Number", String.valueOf(pageNumber));
    }
}
