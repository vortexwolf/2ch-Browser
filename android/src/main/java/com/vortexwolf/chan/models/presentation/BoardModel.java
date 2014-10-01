package com.vortexwolf.chan.models.presentation;

public class BoardModel {
    public BoardModel(String code, String title, boolean isVisible, String group) {
        this.code = code;
        this.title = title;
        this.isVisible = isVisible;
        this.group = group;
    }

    public String code;
    public String title;
    public boolean isVisible;
    public String group;
}
