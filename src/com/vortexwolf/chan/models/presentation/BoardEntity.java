package com.vortexwolf.chan.models.presentation;

public class BoardEntity implements IBoardListEntity {
    private String code;
    private String title;
    private String bump_limit;

    public BoardEntity(String code, String title, String bump_limit) {
        this.code = code;
        this.title = title;
        this.bump_limit = bump_limit;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBumpLimit() {
        return bump_limit;
    }

    public void setBumpLimit(String bump_limit) {
        this.bump_limit = bump_limit;
    }

    @Override
    public boolean isSection() {
        return false;
    }
}
