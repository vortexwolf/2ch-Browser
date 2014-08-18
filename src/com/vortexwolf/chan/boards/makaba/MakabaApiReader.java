package com.vortexwolf.chan.boards.makaba;

import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.makaba.models.MakabaPostInfo;
import com.vortexwolf.chan.boards.makaba.models.MakabaThreadsList;
import com.vortexwolf.chan.exceptions.HtmlNotJsonException;
import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.domain.ThreadModel;
import com.vortexwolf.chan.services.http.HttpStreamReader;
import com.vortexwolf.chan.services.http.JsonReader;

public class MakabaApiReader implements IJsonApiReader {
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
        String uri = this.formatThreadsUri(boardName, page);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }
        
        MakabaThreadsList result = this.mJsonReader.readData(uri, MakabaThreadsList.class, listener, task);
        if (result == null) {
            return null;
        }
        
        ThreadModel[] models = this.mMakabaModelsMapper.mapThreadModels(result);
        return models;
    }

    @Override
    public PostModel[] readPostsList(String boardName, String threadNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.formatPostsUri(boardName, threadNumber);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }
        
        MakabaPostInfo[] result = this.mJsonReader.readData(uri, MakabaPostInfo[].class, listener, task);
        if (result == null) {
            return null;
        }
        
        PostModel[] models = this.mMakabaModelsMapper.mapPostModels(result);
        return models;
    }

    @Override
    public SearchPostListModel searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        // not applicable
        return null;
    }
    
    private String formatThreadsUri(String boardName, int page) {
        String pageName = page == 0 ? "index" : String.valueOf(page);

        return this.mDvachUriBuilder.createBoardUri(boardName, pageName + ".json").toString();
    }

    private String formatPostsUri(String boardName, String threadId) {
        // TODO: add from parameter
        String path = String.format("/makaba/mobile.fcgi?task=get_thread&board=%s&thread=%s&num=%s", boardName, threadId, threadId);
        return this.mDvachUriBuilder.createUri(path).toString();
    }
}
