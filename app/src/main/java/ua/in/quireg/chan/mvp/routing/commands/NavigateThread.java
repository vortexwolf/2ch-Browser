package ua.in.quireg.chan.mvp.routing.commands;

import android.support.annotation.NonNull;

import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 3:11 PM.
 * 2ch-Browser
 */

public class NavigateThread implements Command {

    private String thread;
    private boolean preferDeserialized; //Do not fetch new data upon navigate

    public NavigateThread(@NonNull String thread, boolean preferDeserialized) {
        this.thread = thread;
        this.preferDeserialized = preferDeserialized;
    }

    public String getThread() {
        return thread;
    }

    public boolean isPreferDeserialized() {
        return preferDeserialized;
    }
}
