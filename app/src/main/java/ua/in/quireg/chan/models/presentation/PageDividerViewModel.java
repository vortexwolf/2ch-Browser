package ua.in.quireg.chan.models.presentation;

/**
 * Created by Arcturus Mengsk on 14.04.2019, 12:45.
 * 2ch-Browser
 */
public class PageDividerViewModel implements IThreadListEntity {

    private int mPage = 0;

    @Override
    public Type getType() {
        return Type.DIVIDER;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }
}
