package com.vortexwolf.chan.boards.makaba;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.codehaus.jackson.JsonNode;

import android.content.res.Resources;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.makaba.models.MakabaBoardInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaError;
import com.vortexwolf.chan.boards.makaba.models.MakabaFoundPostsList;
import com.vortexwolf.chan.boards.makaba.models.MakabaPostInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadsList;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadsListCatalog;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.exceptions.HtmlNotJsonException;
import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.models.domain.BoardModel;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.domain.ThreadModel;
import com.vortexwolf.chan.services.IconsList;
import com.vortexwolf.chan.services.http.JsonHttpReader;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class MakabaApiReader implements IJsonApiReader {
    static final String TAG = "MakabaApiReader";

    private final JsonHttpReader mJsonReader;
    private final MakabaUrlBuilder mMakabaUriBuilder;
    private final MakabaModelsMapper mMakabaModelsMapper;
    private final Resources mResources;
    private final ApplicationSettings mApplicationSettings;

    public MakabaApiReader(JsonHttpReader jsonReader, MakabaModelsMapper makabaModelsMapper, MakabaUrlBuilder makabaUriBuilder, Resources resources, ApplicationSettings applicationSettings) {
        this.mJsonReader = jsonReader;
        this.mMakabaModelsMapper = makabaModelsMapper;
        this.mMakabaUriBuilder = makabaUriBuilder;
        this.mResources = resources;
        this.mApplicationSettings = applicationSettings;
    }

    @Override
    public ThreadModel[] readCatalog(String boardName, int filter, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.mMakabaUriBuilder.getCatalogUrlApi(boardName, filter);

        JsonNode json = this.mJsonReader.readData(uri, false, listener, task);
        if (json == null) {
            return null;
        }

        MakabaThreadsListCatalog result = this.parseDataOrThrowError(json, MakabaThreadsListCatalog.class);
        ThreadModel[] models = this.mMakabaModelsMapper.mapCatalog(result);
        return models;
    }

    @Override
    public ThreadModel[] readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.mMakabaUriBuilder.getPageUrlApi(boardName, page);

        JsonNode json = this.mJsonReader.readData(uri, checkModified, listener, task);
        if (json == null) {
            return null;
        }

        MakabaThreadsList result = this.parseDataOrThrowError(json, MakabaThreadsList.class);
        setIcons(result, boardName);

        ThreadModel[] models = this.mMakabaModelsMapper.mapThreadModels(result);
        return models;
    }

    private void setIcons(MakabaThreadsList source, String boardName) {
        try {
            if (source.enable_icons == 1) {
                String[] icons = new String[source.icons.length + 1];
                icons[0] = Factory.resolve(Resources.class).getString(R.string.addpost_politics_default);
                for (int i = 0; i < source.icons.length; ++i)
                    icons[source.icons[i].num] = source.icons[i].name;
                Factory.resolve(IconsList.class).setData(boardName, icons);
            } else Factory.resolve(IconsList.class).setData(boardName, null);
        } catch (Exception e) { MyLog.e(TAG, e); }
    }

    @Override
    public PostModel[] readPostsList(String boardName, String threadNumber, int fromNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        boolean isExtendedUrl = this.mApplicationSettings.isMobileApi() && fromNumber != 0;
        String uri = isExtendedUrl
                ? this.mMakabaUriBuilder.getThreadUrlExtendedApi(boardName, threadNumber, fromNumber + "")
                : this.mMakabaUriBuilder.getThreadUrlApi(boardName, threadNumber);

        JsonNode json = this.mJsonReader.readData(uri, checkModified, listener, task);
        if (json == null) {
            return null;
        }

        MakabaPostInfo[] data = null;
        if (isExtendedUrl) {
            data = this.parseDataOrThrowError(json, MakabaPostInfo[].class);
        } else {
            data = this.parseDataOrThrowError(json, MakabaThreadsList.class).threads[0].posts;
        }

        PostModel[] models = this.mMakabaModelsMapper.mapPostModels(data);
        return models;
    }

//    @Override
//    public BoardModel[] readBoardsList() throws JsonApiReaderException, HtmlNotJsonException {
//        String uri = this.mMakabaUriBuilder.getBoardsUrl();
//        JsonNode json = this.mJsonReader.readData(uri, false, listener, task);
//        MakabaBoardInfo[] data = this.parseDataOrThrowError(json, MakabaBoardInfo[].class);
//        BoardModel[] models = this.mMakabaModelsMapper.mapBoardModels(data);
//        return models;
//    }

    @Override
    public SearchPostListModel searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.mMakabaUriBuilder.getSearchUrlApi();

        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, Constants.MULTIPART_BOUNDARY, Constants.UTF8_CHARSET);
        try {
            entity.addPart("task", new StringBody("search", Constants.UTF8_CHARSET));
            entity.addPart("board", new StringBody(boardName, Constants.UTF8_CHARSET));
            entity.addPart("find", new StringBody(searchQuery, Constants.UTF8_CHARSET));
            entity.addPart("json", new StringBody("1", Constants.UTF8_CHARSET));
        } catch (UnsupportedEncodingException e) {
            MyLog.e(TAG, e);
        }

        JsonNode json = this.mJsonReader.postData(uri, listener, task, entity);
        if (json == null) {
            return null;
        }

        MakabaFoundPostsList result = this.parseDataOrThrowError(json, MakabaFoundPostsList.class);
        SearchPostListModel model = this.mMakabaModelsMapper.mapSearchPostListModel(result);
        return model;
    }

    private <T> T parseDataOrThrowError(JsonNode json, Class<T> valueType) throws JsonApiReaderException {
        T result = this.mJsonReader.convertValue(json, valueType);
        if (result != null) {
            return result;
        }

        MakabaError makabaError = this.mJsonReader.convertValue(json, MakabaError.class);
        if (makabaError != null) {
            String errorCode = makabaError.code == -404 ? "404" : String.valueOf(makabaError.code);
            String errorMessage = makabaError.error != null ? ": " + makabaError.error : "";
            throw new JsonApiReaderException(errorCode + errorMessage);
        }

        throw new JsonApiReaderException(this.mResources.getString(R.string.error_json_parse));
    }
}
