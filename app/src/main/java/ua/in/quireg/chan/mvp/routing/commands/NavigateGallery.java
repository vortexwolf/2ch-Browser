package ua.in.quireg.chan.mvp.routing.commands;

import android.net.Uri;

import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 3:20 PM.
 * 2ch-Browser
 */

public class NavigateGallery implements Command {
    private Uri uri;
    private String threadUrl;

    public NavigateGallery(Uri uri, String threadUrl) {
        this.uri = uri;
        this.threadUrl = threadUrl;
    }

    public Uri getUri() {
        return uri;
    }

    public String getThreadUrl() {
        return threadUrl;
    }
}
