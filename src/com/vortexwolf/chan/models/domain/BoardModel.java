package com.vortexwolf.chan.models.domain;


import com.vortexwolf.chan.boards.makaba.models.MakabaIconInfo;

public class BoardModel {
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



    public String getBump_limit() {
        return bump_limit;
    }

    public void setBump_limit(String bump_limit) {
        this.bump_limit = bump_limit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDefault_name() {
        return default_name;
    }

    public void setDefault_name(String default_name) {
        this.default_name = default_name;
    }

    public int isEnable_likes() {
        return enable_likes;
    }

    public void setEnable_likes(int enable_likes) {
        this.enable_likes = enable_likes;
    }

    public int isEnable_posting() {
        return enable_posting;
    }

    public void setEnable_posting(int enable_posting) {
        this.enable_posting = enable_posting;
    }

    public int isEnable_thread_tags() {
        return enable_thread_tags;
    }

    public void setEnable_thread_tags(int enable_thread_tags) {
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
