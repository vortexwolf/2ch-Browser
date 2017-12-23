package ua.in.quireg.chan.mvp.routing.commands;

import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 2:53 PM.
 * 2ch-Browser
 */

public class NavigateBoard implements Command {

    private String mWebsite;
    private String boardCode; // /mov
    private boolean preferDeserialized; //Do not fetch new data upon navigate

    public NavigateBoard(String mWebsite, String boardCode, boolean preferDeserialized) {
        this.mWebsite = mWebsite;
        this.boardCode = boardCode;
        this.preferDeserialized = preferDeserialized;
    }

    public String getWebsite() {
        return mWebsite;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public boolean isPreferDeserialized() {
        return preferDeserialized;
    }
}
