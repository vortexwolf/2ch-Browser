package com.vortexwolf.chan.services;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.vortexwolf.chan.common.Constants;

public class MyTrackerCompatibility {

    private final Tracker mTracker;

    public MyTrackerCompatibility(Context context) {
        GoogleAnalytics instance = GoogleAnalytics.getInstance(context);
        this.mTracker = instance.getTracker(Constants.ANALYTICS_KEY);

        instance.setDefaultTracker(this.mTracker);
        instance.setDebug(Constants.DEBUG);

        UncaughtExceptionHandler myHandler = new ExceptionReporter(this.mTracker, GAServiceManager.getInstance(), Thread.getDefaultUncaughtExceptionHandler(), context);
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
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

    public void setPageNumberVar(int pageNumber) {
        this.mTracker.setCustomDimension(2, String.valueOf(pageNumber));
    }
}
