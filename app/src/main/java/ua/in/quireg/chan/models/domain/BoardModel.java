package ua.in.quireg.chan.models.domain;


import java.io.Serializable;

import ua.in.quireg.chan.boards.makaba.models.MakabaIconInfo;

public class BoardModel implements Serializable {

    private String bump_limit;
    private String category;
    private String default_name;
    private int enable_likes;
    private int enable_posting;
    private int enable_thread_tags;
    private String id;
    private String name;
    private int pages;
    private int sage;
    private int tripcodes;
    private boolean isVisible;
    private MakabaIconInfo[] icons;


    public String getBumpLimit() {
        return bump_limit;
    }

    public void setBumpLimit(String bump_limit) {
        this.bump_limit = bump_limit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDefaultName() {
        return default_name;
    }

    public void setDefault_name(String default_name) {
        this.default_name = default_name;
    }

    public int isEnableLikes() {
        return enable_likes;
    }

    public void setEnableLikes(int enable_likes) {
        this.enable_likes = enable_likes;
    }

    public int isEnablePosting() {
        return enable_posting;
    }

    public void setEnablePosting(int enable_posting) {
        this.enable_posting = enable_posting;
    }

    public int isEnableThreadTags() {
        return enable_thread_tags;
    }

    public void setEnableThreadTags(int enable_thread_tags) {
        this.enable_thread_tags = enable_thread_tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getSage() {
        return sage;
    }

    public void setSage(int sage) {
        this.sage = sage;
    }

    public int getTripcodes() {
        return tripcodes;
    }

    public void setTripcodes(int tripcodes) {
        this.tripcodes = tripcodes;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public MakabaIconInfo[] getIcons() {
        return icons;
    }

    public void setIcons(MakabaIconInfo[] icons) {
        this.icons = icons;
    }
}
