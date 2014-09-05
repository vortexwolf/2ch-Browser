package com.vortexwolf.chan.boards.dvach;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;

import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;

public class DvachUriParser {
    // 1: board name; 2: page number; 3: thread number
    private static final Pattern groupsDvachUriPattern = Pattern.compile("^/(\\w+)/?(?:(?:(\\d+).html)|(?:res/(\\d+)\\.html))?$");

    public String getBoardName(Uri uri) {
        if (uri.toString().contains("makaba.fcgi?task=catalog")) return uri.toString().substring(uri.toString().lastIndexOf("=") + 1);
        //надо запилить нормальный regex, с учетом того, что board=XX может быть не в конце строки
        String boardName = RegexUtils.getGroupValue(uri, groupsDvachUriPattern, 1);
        return boardName;
    }

    public int getBoardPageNumber(Uri uri) {
        if (uri.toString().contains("makaba.fcgi?task=catalog")) return -1;
        String pageNumber = RegexUtils.getGroupValue(uri, groupsDvachUriPattern, 2);
        return pageNumber == null ? 0 : Integer.parseInt(pageNumber);
    }

    public String getThreadNumber(Uri uri) {
        String threadNumber = RegexUtils.getGroupValue(uri, groupsDvachUriPattern, 3);
        return threadNumber;
    }

    public boolean isThreadUri(Uri uri) {
        String[] groups = RegexUtils.getGroupValues(uri, groupsDvachUriPattern);
        return groups != null && groups.length == 4 && groups[3] != null;
    }

    public boolean isBoardUri(Uri uri) {
        String[] groups = RegexUtils.getGroupValues(uri, groupsDvachUriPattern);
        return groups != null && groups.length == 4 && groups[3] == null;
    }
}
