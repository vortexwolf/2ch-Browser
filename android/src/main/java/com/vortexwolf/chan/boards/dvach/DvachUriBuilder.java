package com.vortexwolf.chan.boards.dvach;

import android.net.Uri;

import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class DvachUriBuilder {
    private final Uri mDvachHostUri;
    private final ApplicationSettings mSettings;
    private final String[] filters = {"standart", "last_reply", "num", "image_size"};

    public DvachUriBuilder(Uri hostUri) {
        this.mDvachHostUri = hostUri;
        this.mSettings = null;
    }

    public DvachUriBuilder(ApplicationSettings settings) {
        this.mSettings = settings;
        this.mDvachHostUri = null;
    }

    public String createPostUri(String board, String threadNumber, String postNumber) {
        return this.createThreadUri(board, threadNumber) + "#" + postNumber;
    }

    public String createThreadUri(String board, String threadNumber) {
        return this.createBoardUri(board, "res/" + threadNumber + ".html").toString();
    }

    public Uri createBoardUri(String board) {
        return this.createBoardUri(board, 0);
    }

    public Uri createBoardUri(String board, int pageNumber) {
        if (pageNumber < 0) {
            String path = String.format("makaba/makaba.fcgi?task=catalog&board=%s&filter=%s", board, filters[-1-pageNumber]);
            return this.createUri(path);
        }
        return this.createBoardUri(board, pageNumber == 0 ? null : pageNumber + ".html");
    }

    public Uri createBoardUri(String board, String path) {
        Uri boardUri = this.appendPath(this.getDvachHostUri(), board);

        if (!StringUtils.isEmpty(path)) {
            boardUri = this.appendPath(boardUri, path);
        }

        return boardUri;
    }

    public Uri createUri(String path) {
        Uri uri = this.appendPath(this.getDvachHostUri(), path);

        return uri;
    }

    public Uri adjustRelativeUri(Uri uri) {
        return uri.isRelative() ? this.createUri(uri.toString()) : uri;
    }

    private Uri appendPath(Uri baseUri, String path) {
        path = path == null ? "" : path.replaceFirst("^/*", "");
        return Uri.withAppendedPath(baseUri, path);
    }

    private Uri getDvachHostUri() {
        if (this.mSettings != null) {
            return this.mSettings.getDomainUri();
        }

        return this.mDvachHostUri;
    }
}
