package ua.in.quireg.chan.mvp.routing.commands;

import android.support.annotation.Nullable;

import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 3:11 PM.
 * 2ch-Browser
 */

public class NavigateThread implements Command {

    private String website;
    private String boardCode; // /mov
    private String thread;
    private String subject;
    private String post;
    private boolean preferDeserialized; //Do not fetch new data upon navigate

    public NavigateThread(String website, String boardCode, String thread, @Nullable String subject, @Nullable String post, boolean preferDeserialized) {
        this.website = website;
        this.boardCode = boardCode;
        this.thread = thread;
        this.subject = subject;
        this.post = post;
        this.preferDeserialized = preferDeserialized;
    }

    public String getWebsite() {
        return website;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getThread() {
        return thread;
    }

    public String getSubject() {
        return subject;
    }

    public String getPost() {
        return post;
    }

    public boolean isPreferDeserialized() {
        return preferDeserialized;
    }
}
