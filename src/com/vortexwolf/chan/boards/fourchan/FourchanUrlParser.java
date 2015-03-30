package com.vortexwolf.chan.boards.fourchan;

import android.net.Uri;

import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.interfaces.IUrlParser;

import java.util.regex.Pattern;

public class FourchanUrlParser implements IUrlParser {
    private static final Pattern groupsFourchanUriPattern = Pattern.compile("^/(\\w+)/?(?:(\\d+)|(?:thread/(\\d+).*))?$");

    public String getBoardName(Uri uri) {
        return RegexUtils.getGroupValue(uri, groupsFourchanUriPattern, 1);
    }

    public int getBoardPageNumber(Uri uri) {
        String pageNumber = RegexUtils.getGroupValue(uri, groupsFourchanUriPattern, 2);
        return pageNumber == null ? 0 : Integer.parseInt(pageNumber);
    }

    public String getThreadNumber(Uri uri) {
        return RegexUtils.getGroupValue(uri, groupsFourchanUriPattern, 3);
    }

    public boolean isThreadUri(Uri uri) {
        String[] groups = RegexUtils.getGroupValues(uri, groupsFourchanUriPattern);
        return groups != null && groups.length == 4 && groups[3] != null;
    }

    public boolean isBoardUri(Uri uri) {
        String[] groups = RegexUtils.getGroupValues(uri, groupsFourchanUriPattern);
        return groups != null && groups.length == 4 && groups[3] == null;
    }

    public String getPostNumber(Uri uri) {
        String fragment = uri.getFragment(); // #p123456
        String postNumber = !StringUtils.isEmpty(fragment) ? fragment.substring(1) : null;
        return postNumber;
    }
}
