package com.vortexwolf.chan.models.presentation;

public class StatusIndicatorEntity implements IPostListEntity {
    private boolean mLoading;
    
    @Override
    public boolean isListItemEnabled() {
        return false;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean mLoading) {
        this.mLoading = mLoading;
    }
}
