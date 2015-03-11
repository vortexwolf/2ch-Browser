package com.vortexwolf.chan.boards.makaba;

import android.net.Uri;

import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class MakabaUriBuilder {
    private static final String[] CATALOG_FILTERS = { "standart", "last_reply", "num", "image_size" };
    private final ApplicationSettings mSettings;

    public MakabaUriBuilder(ApplicationSettings settings) {
        this.mSettings = settings;
    }

    public String getPageUrlApi(String board, int pageNumber) {
        String path = pageNumber == 0
                ? String.format("%s/index.json", board)
                : String.format("%s/%s.json", board, String.valueOf(pageNumber));
        return this.createUri(path).toString();
    }

    public String getThreadUrlApi(String board, String number) {
        String path = String.format("%s/res/%s.json", board, number);
        return this.createUri(path).toString();
    }

    public String getThreadUrlExtendedApi(String board, String number, String from) {
        String path = String.format("/makaba/mobile.fcgi?task=get_thread&board=%s&thread=%s&num=%s&json=1", board, number, !StringUtils.isEmpty(from) ? from : number);
        return this.createUri(path).toString();
    }

    public String getCatalogUrlApi(String board, int filter) {
        String path = String.format("makaba/makaba.fcgi?task=catalog&board=%s&filter=%s&json=1", board, CATALOG_FILTERS[filter]);
        return this.createUri(path).toString();
    }

    public String getSearchUrlApi() {
        return this.createUri("/makaba/makaba.fcgi").toString();
    }

    private Uri createUri(String path) {
        return appendPath(this.mSettings.getDomainUri(), path);
    }

    private Uri appendPath(Uri baseUri, String path) {
        path = path == null ? "" : path.replaceFirst("^/*", "");
        return Uri.withAppendedPath(baseUri, path);
    }
}
