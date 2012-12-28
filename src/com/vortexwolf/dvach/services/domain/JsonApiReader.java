package com.vortexwolf.dvach.services.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.CancellableInputStream;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.library.ProgressInputStream;
import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.models.domain.BoardSettings;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;
import com.vortexwolf.dvach.models.domain.PostsList;
import com.vortexwolf.dvach.models.domain.ThreadsList;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;

public class JsonApiReader implements IJsonApiReader {

    static final String TAG = "JsonApiReader";
    private final DefaultHttpClient mHttpClient;
    private final Resources mResources;
    private final ObjectMapper mObjectMapper;
    private final DvachUriBuilder mDvachUriBuilder;
    private final HashMap<String, String> mIfModifiedMap;

    public JsonApiReader(DefaultHttpClient client, Resources resources, ObjectMapper mapper, DvachUriBuilder dvachUriBuilder) {
        this.mHttpClient = client;
        this.mResources = resources;
        this.mObjectMapper = mapper;
        this.mDvachUriBuilder = dvachUriBuilder;
        this.mIfModifiedMap = new HashMap<String, String>();
    }

    private String formatThreadsUri(String boardName, int page) {
        String pageName = page == 0 ? "wakaba" : String.valueOf(page);

        String uri = this.mDvachUriBuilder.create2chBoardUri(boardName, pageName
                + ".json").toString();

        return uri;
    }

    private String formatPostsUri(String boardName, String threadId) {
        return this.mDvachUriBuilder.create2chBoardUri(boardName, "/res/"
                + threadId + ".json").toString();
    }

    @Override
    public ThreadsList readThreadsList(String boardName, int page, boolean checkModified, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        String uri = formatThreadsUri(boardName, page);

        if (checkModified == false) {
            this.mIfModifiedMap.remove(uri);
        }

        return this.readData(uri, ThreadsList.class, listener, task);
    }

    @Override
    public PostsList readPostsList(String boardName, String threadNumber, boolean checkModified, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        String uri = formatPostsUri(boardName, threadNumber);

        if (checkModified == false) {
            this.mIfModifiedMap.remove(uri);
        }

        return this.readData(uri, PostsList.class, listener, task);
    }

    public <T> T readData(String url, Class<T> valueType, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        return this.readData(url, valueType, listener, task, 0);
    }

    private <T> T readData(String url, Class<T> valueType, IProgressChangeListener listener, ICancelled task, int recLevel) throws JsonApiReaderException {
        HttpGet request = null;
        HttpResponse response = null;
        T result = null;

        try {
            request = this.createRequest(url);

            if (task != null && task.isCancelled()) return null;

            response = this.executeRequest(request);

            // check the response code
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 304) {
                return null;
            }
            if (status.getStatusCode() != 200) {
                throw new JsonApiReaderException(status.getStatusCode() + " - "
                        + status.getReasonPhrase());
            }

            if (task != null && task.isCancelled()) return null;

            // read and parse the response
            InputStream json = this.getInputStream(response, listener, task);

            try {
                result = this.parseResult(json, valueType);
            } catch (JsonParseException e) {
                if (recLevel == 0) {
                    MyLog.v(TAG, "Read json once again");

                    if (listener != null) listener.indeterminateProgress();
                    result = this.readData(url, valueType, null, task, recLevel + 1);
                } else {
                    throw new JsonApiReaderException(mResources.getString(R.string.error_json_parse));
                }
            }

            // save the last modified date
            Header header = response.getFirstHeader("Last-Modified");
            if (header != null) {
                this.mIfModifiedMap.put(url, header.getValue());
            }

            return result;
        } finally {
            ExtendedHttpClient.releaseRequestResponse(request, response);
        }
    }

    private HttpGet createRequest(String url) throws JsonApiReaderException {
        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("Accept", "application/json");
            if (this.mIfModifiedMap.containsKey(url)) {
                request.setHeader("If-Modified-Since", this.mIfModifiedMap.get(url));
            }

            return request;
        } catch (IllegalArgumentException e) {
            MyLog.e(TAG, e);
            throw new JsonApiReaderException(mResources.getString(R.string.error_incorrect_argument), e);
        }
    }

    private HttpResponse executeRequest(HttpGet request) throws JsonApiReaderException {
        try {
            HttpResponse response = this.mHttpClient.execute(request);

            return response;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            throw new JsonApiReaderException(mResources.getString(R.string.error_download_data), e);
        }
    }

    private InputStream getInputStream(HttpResponse response, IProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        HttpEntity entity = response.getEntity();
        try {
            InputStream resultStream = entity.getContent();

            if (listener != null) {
                long contentLength = entity.getContentLength();
                listener.setContentLength(contentLength);

                ProgressInputStream pin = new ProgressInputStream(resultStream, contentLength);
                pin.addProgressChangeListener(listener);
                resultStream = pin;
            }

            if (task != null) {
                resultStream = new CancellableInputStream(resultStream, task);
            }

            return resultStream;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            throw new JsonApiReaderException(mResources.getString(R.string.error_download_data), e);
        }
    }

    private <T> T parseResult(InputStream json, Class<T> valueType) throws JsonParseException, JsonApiReaderException {
        T result = null;
        try {
            result = this.mObjectMapper.readValue(json, valueType);

            return result;
        } catch (JsonParseException e) {
            MyLog.e(TAG, e);
            throw e;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            throw new JsonApiReaderException(mResources.getString(R.string.error_json_parse));
        }
    }
}
