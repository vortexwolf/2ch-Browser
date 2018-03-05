package ua.in.quireg.chan.models.presentation;

import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;

public class OpenTabModel {
    private IWebsite mWebsite;
    private String mBoard;
    private int mPage;
    private String mThread;
    private String mTitle;
    private boolean mIsFavorite;

    private AppearanceUtils.ListViewPosition mPosition;

    public OpenTabModel(IWebsite website, String board, int page, String thread, String title, boolean isFavorite) {
        this.mWebsite = website;
        this.mBoard = board;
        this.mPage = page;
        this.mThread = thread;
        this.mTitle = title;
        this.mIsFavorite = isFavorite;
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

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.mIsFavorite = isFavorite;
    }

    public void setPosition(AppearanceUtils.ListViewPosition position) {
        this.mPosition = position;
    }

    public AppearanceUtils.ListViewPosition getPosition() {
        return this.mPosition;
    }

    public String buildUrl() {
        IUrlBuilder builder = this.mWebsite.getUrlBuilder();
        return UriUtils.getBoardOrThreadUrl(builder, this.mBoard, this.mPage, this.mThread);
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
