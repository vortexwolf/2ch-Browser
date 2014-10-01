package com.vortexwolf.chan.models.presentation;

public class SectionEntity implements IBoardListEntity {
    private final String mTitle;

    public SectionEntity(String title) {
        this.mTitle = title;
    }

    public String getTitle() {
        return this.mTitle;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
