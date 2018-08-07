package ua.in.quireg.chan.models.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "boardModel")
public class BoardModel implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private String mId = "?"; // /mov
    @ColumnInfo(name = "boardName")
    private String mBoardName = "Unknown"; //Movies
    @ColumnInfo(name = "category")
    private String mCategory = "БЕЗНОГNМ"; //Взрослым
    @ColumnInfo(name = "userDefaultName")
    private String mUserDefaultName = "OP"; //Аноним

    @ColumnInfo(name = "pagesCount")
    private int pagesCount = 0;
    @ColumnInfo(name = "bumpLimit")
    private int mBumpLimit = 0;

    @ColumnInfo(name = "likes")
    private boolean likes = false;
    @ColumnInfo(name = "posting")
    private boolean posting = false;
    @ColumnInfo(name = "threadTags")
    private boolean mThreadTags = false;
    @ColumnInfo(name = "sage")
    private boolean mSage = false;
    @ColumnInfo(name = "tripcodes")
    private boolean mTripcodes = false;

    @Ignore
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
        return likes;
    }

    public boolean isPosting() {
        return posting;
    }

    public void setLikes(boolean likes) {
        this.likes = likes;
    }

    public void setPosting(boolean posting) {
        this.posting = posting;
    }

    public String getId() {
        return mId;
    }

    public String getBoardName() {
        return mBoardName;
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
        this.likes = enableLikes;
    }

    public void setEnablePosting(boolean enablePosting) {
        this.posting = enablePosting;
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

    public void setSage(boolean sage) {
        this.mSage = sage;
    }

    public void setTripcodes(boolean tripcodes) {
        this.mTripcodes = tripcodes;
    }

    public void setIcons(BoardIconModel[] icons) {
        if (icons != null) {
            for (BoardIconModel boardIconModel : icons) {
                boardIconModel.setBoard(getBoardName());
            }
            mBoardIconModels = icons;
        }
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }
}
