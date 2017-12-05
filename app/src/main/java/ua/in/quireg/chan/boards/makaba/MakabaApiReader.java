package ua.in.quireg.chan.boards.makaba;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.codehaus.jackson.JsonNode;

import android.content.res.Resources;

import javax.inject.Inject;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.boards.makaba.models.MakabaError;
import ua.in.quireg.chan.boards.makaba.models.MakabaFoundPostsList;
import ua.in.quireg.chan.boards.makaba.models.MakabaPostInfo;
import ua.in.quireg.chan.boards.makaba.models.MakabaThreadsList;
import ua.in.quireg.chan.boards.makaba.models.MakabaThreadsListCatalog;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.exceptions.HtmlNotJsonException;
import ua.in.quireg.chan.exceptions.JsonApiReaderException;
import ua.in.quireg.chan.interfaces.ICancelled;
import ua.in.quireg.chan.interfaces.IJsonApiReader;
import ua.in.quireg.chan.interfaces.IJsonProgressChangeListener;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.SearchPostListModel;
import ua.in.quireg.chan.models.domain.ThreadModel;
import ua.in.quireg.chan.services.IconsList;
import ua.in.quireg.chan.services.http.JsonHttpReader;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class MakabaApiReader implements IJsonApiReader {
    static final String TAG = "MakabaApiReader";

    private final JsonHttpReader mJsonReader;
    private final MakabaUrlBuilder mMakabaUriBuilder;
    private final MakabaModelsMapper mMakabaModelsMapper;
    private final Resources mResources;

    @Inject protected ApplicationSettings mApplicationSettings;

    public MakabaApiReader(JsonHttpReader jsonReader, MakabaModelsMapper makabaModelsMapper, MakabaUrlBuilder makabaUriBuilder, Resources resources) {
        this.mJsonReader = jsonReader;
        this.mMakabaModelsMapper = makabaModelsMapper;
        this.mMakabaUriBuilder = makabaUriBuilder;
        this.mResources = resources;

        MainApplication.getAppComponent().inject(this);
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
