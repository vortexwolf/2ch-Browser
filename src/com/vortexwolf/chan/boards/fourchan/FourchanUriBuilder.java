package com.vortexwolf.chan.boards.fourchan;

public class FourchanUriBuilder {
    public String getPageUrlApi(String board, int pageNumber) {
        return "http://a.4cdn.org/" + board + "/" + (pageNumber + 1) + ".json";
    }

    public String getThreadUrlApi(String board, String number) {
        return "http://a.4cdn.org/" + board + "/thread/" + number + ".json";
    }

    public String getCatalogUrlApi(String board) {
        return "http://a.4cdn.org/" + board + "/catalog.json";
    }
}
