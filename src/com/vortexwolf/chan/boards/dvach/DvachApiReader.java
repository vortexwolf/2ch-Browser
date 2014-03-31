package com.vortexwolf.chan.boards.dvach;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.vortexwolf.chan.boards.dvach.models.DvachFoundPostsList;
import com.vortexwolf.chan.boards.dvach.models.DvachPostsList;
import com.vortexwolf.chan.boards.dvach.models.DvachThreadsList;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.domain.ThreadModel;
import com.vortexwolf.chan.services.http.HttpStreamReader;
import com.vortexwolf.chan.services.http.JsonReader;

public class DvachApiReader implements IJsonApiReader {
    static final String TAG = "DvachApiReader";

    private final HttpStreamReader mHttpStreamReader;
    private final JsonReader mJsonReader;
    private final DvachUriBuilder mDvachUriBuilder;
    private final DvachModelsMapper mDvachModelsMapper;
    
    public DvachApiReader(JsonReader jsonReader, DvachUriBuilder dvachUriBuilder, HttpStreamReader httpStreamReader, DvachModelsMapper dvachModelsMapper) {
        this.mJsonReader = jsonReader;
        this.mDvachUriBuilder = dvachUriBuilder;
        this.mHttpStreamReader = httpStreamReader;
        this.mDvachModelsMapper = dvachModelsMapper;
    }

    @Override
    public SearchPostListModel searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        try {
            searchQuery = URLEncoder.encode(searchQuery, Constants.UTF8_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            MyLog.e(TAG, e);
        }

        // m2-ch.ru
        String uri = String.format("http://91.227.18.102/%s/search?q=%s&out=json&nocheck", boardName, searchQuery);

        DvachFoundPostsList result = this.mJsonReader.readData(uri, DvachFoundPostsList.class, listener, task);
        SearchPostListModel model = this.mDvachModelsMapper.mapSearchPostListModel(result);
        return model;
    }

    @Override
    public ThreadModel[] readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        String uri = this.formatThreadsUri(boardName, page);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }

        DvachThreadsList result = this.mJsonReader.readData(uri, DvachThreadsList.class, listener, task);
        ThreadModel[] models = this.mDvachModelsMapper.mapThreadModels(result);
        return models;
    }

    @Override
    public PostModel[] readPostsList(String boardName, String threadNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        String uri = this.formatPostsUri(boardName, threadNumber);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }

        DvachPostsList result = this.mJsonReader.readData(uri, DvachPostsList.class, listener, task);
        PostModel[] models = this.mDvachModelsMapper.mapPostModels(result.getThread());
        return models;
    }
    
    private String formatThreadsUri(String boardName, int page) {
        String pageName = page == 0 ? "wakaba" : String.valueOf(page);

        return this.mDvachUriBuilder.createBoardUri(boardName, pageName + ".json").toString();
    }

    private String formatPostsUri(String boardName, String threadId) {
        // later test new api
        // String uri = String.format("/makaba/mobile.fcgi?task=get_thread&board=%s&thread=%s&post=0", boardName, threadId);
        return this.mDvachUriBuilder.createBoardUri(boardName, "/res/" + threadId + ".json").toString();
    }
}
