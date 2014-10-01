package com.vortexwolf.chan.models.presentation;

import android.net.Uri;

import com.vortexwolf.chan.common.utils.AppearanceUtils;

public class OpenTabModel {

    private final Uri mUri;
    private String mTitle;

    private AppearanceUtils.ListViewPosition mPosition;

    public OpenTabModel(String title, Uri uri) {
        this.mTitle = title;
        this.mUri = uri;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public void setPosition(AppearanceUtils.ListViewPosition position) {
        this.mPosition = position;
    }

    public AppearanceUtils.ListViewPosition getPosition() {
        return this.mPosition;
    }
}
