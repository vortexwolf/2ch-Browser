package ua.in.quireg.chan.models.domain;

import java.io.Serializable;

public class BoardModel implements Serializable {

    private String mId = "?"; // /mov
    private String mBoardName = "Unknown"; //Movies
    private String mCategory = "БЕЗНОГNМ"; //Взрослым
    private String mUserDefaultName = "OP"; //Аноним

    private int mPagesCount = 0;
    private int mBumpLimit = 0;

    private boolean mLikes = false;
    private boolean mPosting = false;
    private boolean mThreadTags = false;
    private boolean mSage = false;
    private boolean mTripcodes = false;
    private BoardIconModel[] mBoardIconModels = new BoardIconModel[1];

    public int getBumpLimit() {
        return mBumpLimit;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getUserDefaultName() {
        return mUserDefaultName;
    }

    public boolean isThreadTags() {
        return mThreadTags;
    }

    public boolean isLikes() {
        return mLikes;
    }

    public boolean isPosting() {
        return mPosting;
    }

    public String getId() {
        return mId;
    }

    public String getBoardName() {
        return mBoardName;
    }

    public int getPages() {
        return mPagesCount;
    }

    public boolean isSage() {
        return mSage;
    }

    public boolean isTripcodes() {
        return mTripcodes;
    }

    public BoardIconModel[] getIcons() {
        return mBoardIconModels;
    }

    public void setBumpLimit(int bumpLimit) {
        this.mBumpLimit = bumpLimit;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    public void setUserDefaultName(String userDefaultName) {
        this.mUserDefaultName = userDefaultName;
    }

    public void setEnableLikes(boolean enableLikes) {
        this.mLikes = enableLikes;
    }

    public void setEnablePosting(boolean enablePosting) {
        this.mPosting = enablePosting;
    }

    public void setThreadTags(boolean threadTags) {
        this.mThreadTags = threadTags;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setBoardName(String name) {
        this.mBoardName = name;
    }

    public void setPages(int pages) {
        this.mPagesCount = pages;
    }

    public void setSage(boolean sage) {
        this.mSage = sage;
    }

    public void setTripcodes(boolean tripcodes) {
        this.mTripcodes = tripcodes;
    }

    public void setIcons(BoardIconModel[] icons) {
        this.mBoardIconModels = icons;
    }

}
