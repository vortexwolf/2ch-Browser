package ua.in.quireg.chan.mvp.routing.commands;

import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 12:35 PM.
 * 2ch-Browser
 */

public class SetSupportActionBarTitle implements Command {

    private String title;

    public SetSupportActionBarTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
