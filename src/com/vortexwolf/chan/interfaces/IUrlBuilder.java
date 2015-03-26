package com.vortexwolf.chan.interfaces;

public interface IUrlBuilder {

    public abstract String getPageUrlApi(String board, int pageNumber);

    public abstract String getPageUrlHtml(String board, int pageNumber);

    public abstract String getThreadUrlApi(String board, String number);

    public abstract String getThreadUrlHtml(String board, String threadNumber);

    public abstract String getPostUrlHtml(String board, String threadNumber, String postNumber);

    public abstract String getCatalogUrlApi(String board, int filter);

    public abstract String getCatalogUrlHtml(String board, int filter);

    public abstract String getIconUrl(String path);

    public abstract String getThumbnailUrl(String board, String path);

    public abstract String getImageUrl(String board, String path);

    public abstract String makeAbsolute(String url);

    public abstract String getPostingUrlHtml();

    public abstract String getPostingUrlApi();

    public abstract String getCloudflareCheckUrl(String key, String answer);

    public abstract String getPasscodeCheckUrl();

    public abstract String getPasscodeCookieCheckUrl(String passcodeCookie);

}