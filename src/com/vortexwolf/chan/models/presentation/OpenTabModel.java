package com.vortexwolf.chan.models.presentation;

import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IWebsite;

public class OpenTabModel {
    private IWebsite mWebsite;
    private String mBoard;
    private int mPage;
    private String mThread;
    private String mTitle;

    private AppearanceUtils.ListViewPosition mPosition;

    public OpenTabModel(IWebsite website, String board, int page, String thread, String title) {
        this.mWebsite = website;
        this.mBoard = board;
        this.mPage = page;
        this.mThread = thread;
        this.mTitle = title;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public IWebsite getWebsite() {
        return this.mWebsite;
    }

    public String getBoard() {
        return this.mBoard;
    }

    public int getPage() {
        return this.mPage;
    }

    public String getThread() {
        return this.mThread;
    }

    public void setPosition(AppearanceUtils.ListViewPosition position) {
        this.mPosition = position;
    }

    public AppearanceUtils.ListViewPosition getPosition() {
        return this.mPosition;
    }

    public String buildUrl() {
        IUrlBuilder builder = this.mWebsite.getUrlBuilder();
        return UriUtils.getBoardOrThreadUrl(builder, this.mBoard, this.mThread);
    }

    public String getTitleOrDefault() {
        if (!StringUtils.isEmpty(this.mTitle)) {
            return this.mTitle;
        } else if (StringUtils.isEmpty(this.mThread)) {
            return this.mBoard;
        } else {
            return this.mBoard + "/" + this.mThread;
        }
    }

    public boolean isEqualTo(OpenTabModel tab) {
        return isEqualTo(tab.mWebsite, tab.mBoard, tab.mThread, tab.mPage);
    }

    public boolean isEqualTo(IWebsite website, String board, String thread, int page) {
        return StringUtils.areEqual(this.mWebsite.name(), website.name()) &&
                StringUtils.areEqual(this.mBoard, board) &&
                StringUtils.areEqual(this.mThread, thread) &&
                this.mPage == page;
    }
}
