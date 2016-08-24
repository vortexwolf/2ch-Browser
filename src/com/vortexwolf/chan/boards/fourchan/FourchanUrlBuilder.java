package com.vortexwolf.chan.boards.fourchan;

import android.net.Uri;

import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.models.domain.CaptchaType;

public class FourchanUrlBuilder implements IUrlBuilder {
    public String getPageUrlApi(String board, int pageNumber) {
        return "http://a.4cdn.org/" + board + "/" + (pageNumber + 1) + ".json";
    }

    public String getPageUrlHtml(String board, int pageNumber) {
        return "http://boards.4chan.org/" + board + (pageNumber == 0 ? "" : "/" + pageNumber);
    }

    public String getThreadUrlApi(String board, String number) {
        return "http://a.4cdn.org/" + board + "/thread/" + number + ".json";
    }

    public String getThreadUrlHtml(String board, String threadNumber) {
        return "http://boards.4chan.org/" + board + "/thread/" + threadNumber;
    }

    public String getPostUrlHtml(String board, String threadNumber, String postNumber) {
        return this.getThreadUrlHtml(board, threadNumber) + "#p" + postNumber;
    }

    public String getCatalogUrlApi(String board, int filter) {
        return "http://a.4cdn.org/" + board + "/catalog.json";
    }

    public String getIconUrl(String path) {
        return "http://s.4cdn.org/image/" + path;
    }

    public String getThumbnailUrl(String board, String path) {
        return "http://t.4cdn.org/" + board + "/" + path;
    }

    public String getImageUrl(String board, String path) {
        return "http://i.4cdn.org/" + board + "/" + path;
    }

    public String getCatalogUrlHtml(String board, int filter) {
        return this.getPageUrlHtml(board, 0) + "/catalog";
    }

    public String makeAbsolute(String url) {
        Uri uri = Uri.parse(url);
        uri = uri.isRelative() ? this.appendPath(Uri.parse("http://boards.4chan.org"), url) : uri;
        return uri.toString();
    }

    private Uri appendPath(Uri baseUri, String path) {
        path = path == null ? "" : path.replaceFirst("^/*", "");
        return Uri.withAppendedPath(baseUri, path);
    }

    public String getPostingUrlHtml() {
        return null;
    }

    public String getPostingUrlApi() {
        return null;
    }

    public String getCloudflareCheckUrl(String key, String answer) {
        return null;
    }

    public String getPasscodeCheckUrl() {
        return null;
    }

    public String getPasscodeCookieCheckUrl(String boardName, String threadNumber) {
        return null;
    }

    @Override
    public String getAppCaptchaCheckUrl(String public_key) {
        return null;
    }

}
