package com.vortexwolf.chan.db;

public class UrlTitleEntity {
    private long id;
    private String title;
    private String url;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public String toString() {
        return this.id + " " + this.title;
    }
}
