package com.vortexwolf.dvach.services.presentation;

import android.net.Uri;

import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class DvachUriBuilder {
    private final Uri mDvachHostUri;
    private final ApplicationSettings mSettings;

    public DvachUriBuilder(Uri hostUri) {
        this.mDvachHostUri = hostUri;
        this.mSettings = null;
    }

    public DvachUriBuilder(ApplicationSettings settings) {
        this.mSettings = settings;
        this.mDvachHostUri = null;
    }

    public String create2chPostUrl(String board, String threadNumber, String postNumber) {
        return this.create2chThreadUrl(board, threadNumber) + "#" + postNumber;
    }

    public String create2chThreadUrl(String board, String threadNumber) {
        return this.create2chBoardUri(board, "res/" + threadNumber + ".html").toString();
    }
    
    public String create2chThreadUrl(String board, String threadNumber, String postNumber) {
        return this.create2chThreadUrl(board, threadNumber) + "#" + postNumber;
    }

    public Uri create2chBoardUri(String board) {
        return this.create2chBoardUri(board, 0);
    }
    
    public Uri create2chBoardUri(String board, int pageNumber) {
        return this.create2chBoardUri(board, pageNumber == 0 ? null : pageNumber + ".html");
    }

    public Uri create2chBoardUri(String board, String path) {
        Uri boardUri = this.appendPath(this.getDvachHostUri(), board);

        if (!StringUtils.isEmpty(path)) {
            boardUri = this.appendPath(boardUri, path);
        }

        return boardUri;
    }

    public Uri create2chUri(String path) {
        Uri uri = this.appendPath(this.getDvachHostUri(), path);

        return uri;
    }

    public Uri adjust2chRelativeUri(Uri uri) {
        return uri.isRelative() ? this.appendPath(this.getDvachHostUri(), uri.toString()) : uri;
    }

    private Uri appendPath(Uri baseUri, String path) {
        path = path.replaceFirst("^/*", "");
        return Uri.withAppendedPath(baseUri, path);
    }

    private Uri getDvachHostUri() {
        if (this.mSettings != null) {
            return this.mSettings.getDomainUri();
        }

        return this.mDvachHostUri;
    }
}
