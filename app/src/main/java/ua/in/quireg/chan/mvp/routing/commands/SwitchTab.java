package ua.in.quireg.chan.mvp.routing.commands;

import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 2:05 PM.
 * 2ch-Browser
 */

public class SwitchTab implements Command {

    private int tabPosition;

    public SwitchTab(int tabPosition) {
        this.tabPosition = tabPosition;
    }

    public int getTabPosition() {
        return tabPosition;
    }
}
