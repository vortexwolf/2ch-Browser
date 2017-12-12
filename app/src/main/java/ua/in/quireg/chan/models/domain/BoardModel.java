package ua.in.quireg.chan.models.domain;


import android.support.annotation.NonNull;

import java.io.Serializable;

import ua.in.quireg.chan.boards.makaba.models.MakabaIconInfo;
import ua.in.quireg.chan.models.presentation.IBoardListEntity;

public class BoardModel implements Serializable, IBoardListEntity {

    private String id; // /mov
    private String name; //Movies
    private String bumpLimit; //500
    private String category;
    private String defaultName;
    private int enableLikes;
    private int enablePosting;
    private int enableThreadTags;
    private int pages;
    private int sage;
    private int tripcodes;
    private MakabaIconInfo[] icons;

    @NonNull
    public String getBumpLimit() {
        if (bumpLimit == null) {
            return "?";
        }
        return bumpLimit;
    }
    @NonNull
    public String getCategory() {
        if (category == null) {
            return "?";
        }
        return category;
    }
    @NonNull
    public String getDefaultName() {
        if (defaultName == null) {
            return "?";
        }
        return defaultName;
    }

    public int isEnableLikes() {
        return enableLikes;
    }

    public int isEnablePosting() {
        return enablePosting;
    }

    public int isEnableThreadTags() {
        return enableThreadTags;
    }
    @NonNull
    public String getId() {
        if (id == null) {
            return "-1";
        }
        return id;
    }
    @NonNull
    public String getName() {
        if (name == null) {
            return "Unknown";
        }
        return name;
    }

    public int getPages() {
        return pages;
    }

    public int getSage() {
        return sage;
    }

    public int getTripcodes() {
        return tripcodes;
    }

    @NonNull
    public MakabaIconInfo[] getIcons() {
        if (icons == null) {
            return new MakabaIconInfo[1];
        }
        return icons;
    }

    @Override
    public boolean isSection() {
        return false;
    }


    public void setBumpLimit(String bump_limit) {
        this.bumpLimit = bump_limit;
    }


    public void setCategory(String category) {
        this.category = category;
    }


    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }


    public void setEnableLikes(int enable_likes) {
        this.enableLikes = enable_likes;
    }


    public void setEnablePosting(int enable_posting) {
        this.enablePosting = enable_posting;
    }


    public void setEnableThreadTags(int enable_thread_tags) {
        this.enableThreadTags = enable_thread_tags;
    }


    public void setId(String id) {
        this.id = id;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setPages(int pages) {
        this.pages = pages;
    }


    public void setSage(int sage) {
        this.sage = sage;
    }


    public void setTripcodes(int tripcodes) {
        this.tripcodes = tripcodes;
    }


    public void setIcons(MakabaIconInfo[] icons) {
        this.icons = icons;
    }

}
