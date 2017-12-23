package ua.in.quireg.chan.models.presentation;

import android.support.annotation.NonNull;

public class SectionEntity implements BoardsListEntity {

    private final String mTitle;

    public SectionEntity(@NonNull String title) {
        mTitle = title;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
