package com.vortexwolf.chan.boards.makaba;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import android.content.res.Resources;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.makaba.models.MakabaError;
import com.vortexwolf.chan.boards.makaba.models.MakabaFoundPostsList;
import com.vortexwolf.chan.boards.makaba.models.MakabaPostInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadsList;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadsListCatalog;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.exceptions.HtmlNotJsonException;
import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.domain.ThreadModel;
import com.vortexwolf.chan.services.IconsList;
import com.vortexwolf.chan.services.http.HttpStreamReader;
import com.vortexwolf.chan.services.http.JsonReader;

public class MakabaApiReader implements IJsonApiReader {
    static final String TAG = "MakabaApiReader";
    private final HttpStreamReader mHttpStreamReader;
    private final JsonReader mJsonReader;
    private final DvachUriBuilder mDvachUriBuilder;
    private final MakabaModelsMapper mMakabaModelsMapper;
    
    public MakabaApiReader(JsonReader jsonReader, DvachUriBuilder dvachUriBuilder, HttpStreamReader httpStreamReader, MakabaModelsMapper makabaModelsMapper) {
        this.mJsonReader = jsonReader;
        this.mDvachUriBuilder = dvachUriBuilder;
        this.mHttpStreamReader = httpStreamReader;
        this.mMakabaModelsMapper = makabaModelsMapper;
    }
    
    @Override
    public ThreadModel[] readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        if (page < 0) return readCatalog(boardName, page, checkModified, listener, task);
        String uri = this.formatThreadsUri(boardName, page);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }
        
        MakabaThreadsList result = this.mJsonReader.readData(uri, MakabaThreadsList.class, listener, task);
        if (result == null) {
            return null;
        }
        
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
    
    private ThreadModel[] readCatalog(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = mDvachUriBuilder.createBoardUri(boardName, page).toString() + "&json=1";
        
        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }
        
        MakabaThreadsListCatalog result = this.mJsonReader.readData(uri, MakabaThreadsListCatalog.class, listener, task);
        if (result == null) {
            return null;
        }
        
        ThreadModel[] models = this.mMakabaModelsMapper.mapThreadModels(result);
        return models;
    }

    @Override
    public PostModel[] readPostsList(String boardName, String threadNumber, String fromNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.formatPostsUri(boardName, threadNumber, fromNumber);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }
        
        MakabaPostInfo[] result = null;
        try {
            result = this.mJsonReader.readData(uri, MakabaPostInfo[].class, listener, task);
        } catch (JsonApiReaderException e) {
            MakabaError makabaError = null;
            try {
                makabaError = this.mJsonReader.readData(uri, MakabaError.class, listener, task);
            } catch (Exception ex) {
                MyLog.e(TAG, ex);
            }
            if (makabaError != null) {
                String error = makabaError.code == -404 ? "404" : Integer.toString(makabaError.code);
                if (makabaError.error != null) error += ": " + makabaError.error;
                throw new JsonApiReaderException(error);
            } else throw e;
        }
        if (result == null) {
            return null;
        }
        
        PostModel[] models = this.mMakabaModelsMapper.mapPostModels(result);
        return models;
    }

    @Override
    public SearchPostListModel searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.mDvachUriBuilder.createUri("/makaba/makaba.fcgi").toString();
        
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, Constants.MULTIPART_BOUNDARY, Constants.UTF8_CHARSET);
        try {
            entity.addPart("task", new StringBody("search", Constants.UTF8_CHARSET));
            entity.addPart("board", new StringBody(boardName, Constants.UTF8_CHARSET));
            entity.addPart("find", new StringBody(searchQuery, Constants.UTF8_CHARSET));
            entity.addPart("json", new StringBody("1", Constants.UTF8_CHARSET));
        } catch (UnsupportedEncodingException e) {
            MyLog.e(TAG, e);
        }
        
        MakabaFoundPostsList result = this.mJsonReader.readData(uri, MakabaFoundPostsList.class, listener, task, true, entity);
        if (result == null) {
            return null;
        }

        SearchPostListModel model = this.mMakabaModelsMapper.mapSearchPostListModel(result);
        return model;
    }
    
    private String formatThreadsUri(String boardName, int page) {
        String pageName = page == 0 ? "index" : String.valueOf(page);

        return this.mDvachUriBuilder.createBoardUri(boardName, pageName + ".json").toString();
    }

    private String formatPostsUri(String boardName, String threadId, String fromId) {
        String path = String.format("/makaba/mobile.fcgi?task=get_thread&board=%s&thread=%s&num=%s", boardName, threadId, !StringUtils.isEmpty(fromId) ? fromId : threadId);
        return this.mDvachUriBuilder.createUri(path).toString();
    }
}
