package ua.in.quireg.chan.boards.makaba;

import java.util.regex.Pattern;

import android.net.Uri;

import ua.in.quireg.chan.common.utils.RegexUtils;
import ua.in.quireg.chan.interfaces.IUrlParser;

public class MakabaUrlParser implements IUrlParser {
    private static final Pattern groupsDvachUriPattern = Pattern.compile("^/(\\w+)/?(?:(?:(\\d+).html)|(?:res/(\\d+)\\.html))?$");

    public String getBoardName(Uri uri) {
        String boardName = RegexUtils.getGroupValue(uri, groupsDvachUriPattern, 1);
        return boardName;
    }

    public int getBoardPageNumber(Uri uri) {
        String pageNumber = RegexUtils.getGroupValue(uri, groupsDvachUriPattern, 2);
        return pageNumber == null ? 0 : Integer.parseInt(pageNumber);
    }

    public String getThreadNumber(Uri uri) {
        String threadNumber = RegexUtils.getGroupValue(uri, groupsDvachUriPattern, 3);
        return threadNumber;
    }

    public String getPostNumber(Uri uri) {
        String postNumber = uri.getFragment();
        return postNumber;
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
