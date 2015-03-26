package com.vortexwolf.chan.interfaces;

import android.net.Uri;

public interface IUrlParser {

    public abstract String getBoardName(Uri uri);

    public abstract int getBoardPageNumber(Uri uri);

    public abstract String getThreadNumber(Uri uri);

    public abstract boolean isThreadUri(Uri uri);

    public abstract boolean isBoardUri(Uri uri);

    public abstract String getPostNumber(Uri uri);

}