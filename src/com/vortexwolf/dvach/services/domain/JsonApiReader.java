package com.vortexwolf.dvach.services.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.res.Resources;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.library.CancellableInputStream;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.library.ProgressInputStream;
import com.vortexwolf.dvach.exceptions.HttpRequestException;
import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.models.domain.HttpStreamModel;
import com.vortexwolf.dvach.models.domain.PostsList;
import com.vortexwolf.dvach.models.domain.ThreadsList;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;

public class JsonApiReader implements IJsonApiReader {

    static final String TAG = "JsonApiReader";
    private final DefaultHttpClient mHttpClient;
    private final Resources mResources;
    private final ObjectMapper mObjectMapper;
    private final DvachUriBuilder mDvachUriBuilder;
    private final HttpStreamReader mHttpStreamReader;

    public JsonApiReader(DefaultHttpClient client, Resources resources, ObjectMapper mapper, DvachUriBuilder dvachUriBuilder) {
        this.mHttpClient = client;
        this.mResources = resources;
        this.mObjectMapper = mapper;
        this.mDvachUriBuilder = dvachUriBuilder;
        this.mHttpStreamReader = new HttpStreamReader(this.mHttpClient, resources);
    }

    private String formatThreadsUri(String boardName, int page) {
        String pageName = page == 0 ? "wakaba" : String.valueOf(page);

        String uri = this.mDvachUriBuilder.create2chBoardUri(boardName, pageName + ".json").toString();

        return uri;
    }

    private String formatPostsUri(String boardName, String threadId) {
        return this.mDvachUriBuilder.create2chBoardUri(boardName, "/res/" + threadId + ".json").toString();
    }

    @Override
    public ThreadsList readThreadsList(String boardName, int page, boolean checkModified, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        String uri = this.formatThreadsUri(boardName, page);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }

        return this.readData(uri, ThreadsList.class, listener, task);
    }

    @Override
    public PostsList readPostsList(String boardName, String threadNumber, boolean checkModified, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        String uri = this.formatPostsUri(boardName, threadNumber);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }

        return this.readData(uri, PostsList.class, listener, task);
    }

    public <T> T readData(String url, Class<T> valueType, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        T result = null;
        HttpStreamModel streamModel = null;
        boolean parseSuccess = false;
        boolean wasCancelled = false;

        for(int i = 0; i < 2 && !parseSuccess && !wasCancelled; i++) {
            try {
                streamModel = this.mHttpStreamReader.fromUri(url, null, listener, task);
            } catch (HttpRequestException e) {
                throw new JsonApiReaderException(e.getMessage());
            }
            
            if (streamModel.stream == null || task != null && task.isCancelled()) {
                wasCancelled = true;
                break;
            }
    
            try {
                result = this.mObjectMapper.readValue(streamModel.stream, valueType);
                parseSuccess = true;
            } catch (JsonParseException e) {
                if(task != null && task.isCancelled()) {
                    wasCancelled = true;
                    break;
                }
                
                MyLog.e(TAG, e);
                MyLog.v(TAG, "Read json once again");

                this.mHttpStreamReader.removeIfModifiedForUri(url);
                
                if (listener != null) {
                    listener.indeterminateProgress();
                }
            } catch (Exception e) {
                break;
            }
        }
        
        ExtendedHttpClient.releaseRequestResponse(streamModel.request, streamModel.response);
        
        if(!parseSuccess && !wasCancelled) {
            throw new JsonApiReaderException(this.mResources.getString(R.string.error_json_parse));
        }

        return result;
    }
}
