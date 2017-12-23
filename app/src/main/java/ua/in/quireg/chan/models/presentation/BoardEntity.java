package ua.in.quireg.chan.models.presentation;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 4:21 AM.
 * 2ch-Browser
 */

public class BoardEntity implements BoardsListEntity {

    public String id = "?"; // /mov
    public String boardName = "Unknown"; //Movies
    public String category = "БЕЗНОГNМ"; //Взрослым
    public int bumpLimit = 0;

    @Override
    public boolean isSection() {
        return false;
    }
}
