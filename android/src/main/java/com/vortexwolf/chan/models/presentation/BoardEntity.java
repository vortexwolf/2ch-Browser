package com.vortexwolf.chan.models.presentation;

public class BoardEntity implements IBoardListEntity {
    private String code;
    private String title;

    public BoardEntity(String code, String title) {
        this.code = code;
        this.title = title;
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

    @Override
    public boolean isSection() {
        return false;
    }
}
