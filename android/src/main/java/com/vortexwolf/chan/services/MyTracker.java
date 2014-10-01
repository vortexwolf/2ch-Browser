package com.vortexwolf.chan.services;

import android.content.Context;

import com.vortexwolf.chan.common.Constants;

public class MyTracker {

    private MyTrackerCompatibility m;
    
    public MyTracker(Context context) {
        if (Constants.SDK_VERSION >= 4) {
            m = new MyTrackerCompatibility(context);
        }
    }

    // tracking methods
    public void trackActivityView(String pagePath) {
        if (Constants.SDK_VERSION >= 4) {
            m.trackActivityView(pagePath);
        }
    }

    public void setBoardVar(String boardName) {
        if (Constants.SDK_VERSION >= 4) {
            m.setBoardVar(boardName);
        }
    }

    public void setPageNumberVar(int pageNumber) {
        if (Constants.SDK_VERSION >= 4) {
            m.setPageNumberVar(pageNumber);
        }
    }
}
