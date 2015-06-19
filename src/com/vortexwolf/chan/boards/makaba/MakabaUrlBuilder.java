package com.vortexwolf.chan.boards.makaba;

import android.net.Uri;

import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class MakabaUrlBuilder implements IUrlBuilder {
    private static final String[] CATALOG_FILTERS = { "catalog", "catalog_num" };
    private final ApplicationSettings mSettings;

    public MakabaUrlBuilder(ApplicationSettings settings) {
        this.mSettings = settings;
    }

    public String getPageUrlApi(String board, int pageNumber) {
        String path = pageNumber == 0
                ? String.format("%s/index.json", board)
                : String.format("%s/%s.json", board, String.valueOf(pageNumber));
        return this.createRootUri(path).toString();
    }

    public String getPageUrlHtml(String board, int pageNumber) {
        return this.createBoardUri(board, pageNumber == 0 ? null : pageNumber + ".html").toString();
    }

    public String getThreadUrlApi(String board, String number) {
        String path = String.format("%s/res/%s.json", board, number);
        return this.createRootUri(path).toString();
    }

    public String getThreadUrlExtendedApi(String board, String number, String from) {
        String path = String.format("makaba/mobile.fcgi?task=get_thread&board=%s&thread=%s&num=%s&json=1", board, number, !StringUtils.isEmpty(from) ? from : number);
        return this.createRootUri(path).toString();
    }

    public String getThreadUrlHtml(String board, String threadNumber) {
        return this.createBoardUri(board, "res/" + threadNumber + ".html").toString();
    }

    public String getPostUrlHtml(String board, String threadNumber, String postNumber) {
        return this.getThreadUrlHtml(board, threadNumber) + "#" + postNumber;
    }

    public String getCatalogUrlApi(String board, int filter) {
        return this.createBoardUri(board, CATALOG_FILTERS[filter] + ".json").toString();
    }

    public String getCatalogUrlHtml(String board, int filter) {
        return this.createBoardUri(board, CATALOG_FILTERS[filter] + ".html").toString();
    }

    public String getSearchUrlApi() {
        return this.createRootUri("makaba/makaba.fcgi").toString();
    }

    public String getIconUrl(String path) {
        return this.createRootUri(path).toString();
    }

    public String getThumbnailUrl(String board, String path) {
        return this.createBoardUri(board, path).toString();
    }

    public String getImageUrl(String board, String path) {
        return this.createBoardUri(board, path).toString();
    }

    public String getPostingUrlApi() {
        return this.createRootUri("makaba/posting.fcgi?json=1").toString();
    }

    public String getPostingUrlHtml() {
        return this.createRootUri("makaba/posting.fcgi").toString();
    }

    public String getCloudflareCheckUrl(String challenge, String response) {
        return this.createRootUri("cdn-cgi/l/chk_captcha" + "?recaptcha_challenge_field=" + challenge + "&recaptcha_response_field=" + response).toString();
    }

    public String getPasscodeCheckUrl() {
        return this.createRootUri("makaba/makaba.fcgi").toString();
    }

    public String getPasscodeCookieCheckUrl(String passcodeCookie) {
        String passcodeParameter = !StringUtils.isEmpty(passcodeCookie) ? "?usercode=" + passcodeCookie : "";
        return this.createRootUri("makaba/captcha.fcgi" + passcodeParameter).toString();
    }

    public String makeAbsolute(String url) {
        Uri uri = Uri.parse(url);
        uri = uri.isRelative() ? this.createRootUri(url) : uri;
        return uri.toString();
    }

    private Uri createBoardUri(String board, String path) {
        return this.appendPath(this.createRootUri(board), path);
    }

    private Uri createRootUri(String path) {
        return this.appendPath(this.mSettings.getDomainUri(), path);
    }

    private Uri appendPath(Uri baseUri, String path) {
        path = path == null ? "" : path.replaceFirst("^/*", "");
        return Uri.withAppendedPath(baseUri, path);
    }
}
