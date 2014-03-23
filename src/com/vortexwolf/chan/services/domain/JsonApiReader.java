package com.vortexwolf.chan.services.domain;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.CancellableInputStream;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.library.ProgressInputStream;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.interfaces.IProgressChangeListener;
import com.vortexwolf.chan.models.domain.FoundPostsList;
import com.vortexwolf.chan.models.domain.HttpStreamModel;
import com.vortexwolf.chan.models.domain.PostsList;
import com.vortexwolf.chan.models.domain.ThreadsList;
import com.vortexwolf.chan.services.presentation.DvachUriBuilder;

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
        // later test new api
        // String uri = String.format("/makaba/mobile.fcgi?task=get_thread&board=%s&thread=%s&post=0", boardName, threadId);
        return this.mDvachUriBuilder.create2chBoardUri(boardName, "/res/" + threadId + ".json").toString();
    }

    @Override
    public FoundPostsList searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        try {
            searchQuery = URLEncoder.encode(searchQuery, Constants.UTF8_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            MyLog.e(TAG, e);
        }
        
        // m2-ch.ru
        String uri = String.format("http://91.227.18.102/%s/search?q=%s&out=json&nocheck", boardName, searchQuery);
        
        return this.readData(uri, FoundPostsList.class, listener, task);
    }
    
    @Override
    public ThreadsList readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        String uri = this.formatThreadsUri(boardName, page);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }

        return this.readData(uri, ThreadsList.class, listener, task);
    }

    @Override
    public PostsList readPostsList(String boardName, String threadNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        String uri = this.formatPostsUri(boardName, threadNumber);

        if (checkModified == false) {
            this.mHttpStreamReader.removeIfModifiedForUri(uri);
        }

        return this.readData(uri, PostsList.class, listener, task);
    }

    public <T> T readData(String url, Class<T> valueType, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException {
        T result = null;
        boolean parseSuccess = false;
        boolean wasCancelled = false;

        for(int i = 0; i < 2; i++) {
            try {
                result = this.tryReadAndParse(url, valueType, listener, task);
                parseSuccess = true;
                break;
            } catch (HttpRequestException e) {
                throw new JsonApiReaderException(e.getMessage());
            } catch (NotJsonException e) {
                throw new JsonApiReaderException(this.mResources.getString(R.string.error_not_json));
            } catch (CancelTaskException e) {
                wasCancelled = true;
                break;
            } catch (JsonParseException e) {
                // try to load once again
                continue;
            } catch (Exception e) {
                MyLog.e(TAG, e);
                break;
            }
        }

        if(task != null && task.isCancelled()) {
            wasCancelled = true;
        }
        
        if(!parseSuccess && !wasCancelled) {
            throw new JsonApiReaderException(this.mResources.getString(R.string.error_json_parse));
        }

        return result;
    }
    
    private <T> T tryReadAndParse(String url, Class<T> valueType, IJsonProgressChangeListener listener, ICancelled task) throws HttpRequestException, CancelTaskException, IOException, NotJsonException{
        HttpStreamModel streamModel = null;
        try {
            streamModel = this.mHttpStreamReader.fromUri(url, null, listener, task);
            
            if (streamModel.notModifiedResult || streamModel.stream == null) {
                throw new CancelTaskException();
            }
            
            byte[] bytes = this.readBytes(streamModel.stream, listener);
            
            if (task != null && task.isCancelled()) {
                throw new CancelTaskException();
            }
            
            if (bytes.length > 20) {
                String firstLetters = new String(bytes, 0, 20, Constants.UTF8_CHARSET.name());
                // check if it is an html page instead of a json page
                if(firstLetters != null && firstLetters.toLowerCase().startsWith("<!doctype")) {
                    throw new NotJsonException();
                }
            }
    
            InputStream memoryStream = this.createStreamForParsing(bytes, listener, task);
            
            try {
                T result = this.mObjectMapper.readValue(memoryStream, valueType);
                return result;
            } catch (JsonParseException e) {
                if(task != null && task.isCancelled()) {
                    throw new CancelTaskException();
                }
                
                MyLog.e(TAG, e);
                MyLog.v(TAG, "Read json once again");
    
                this.mHttpStreamReader.removeIfModifiedForUri(url);
                
                if (listener != null) {
                    listener.indeterminateProgress();
                }
                
                throw e;
            }
        } finally {
            if(streamModel != null) {
                ExtendedHttpClient.releaseRequestResponse(streamModel.request, streamModel.response);
            }
        }
    }
    
    private byte[] readBytes(InputStream stream, IJsonProgressChangeListener listener) throws IOException {
        double scale = 1;
        long oldContentLength = listener.getContentLength();
        long newContentLength = oldContentLength + (long)(oldContentLength * scale);
        
        // increase the progress bar length, so that the reading stopped at 50%
        listener.setContentLength(newContentLength);
        
        byte[] bytes = IoUtils.convertStreamToBytes(stream);
        
        listener.setOffsetAndScale(oldContentLength, scale);
        
        return bytes;
    }
    
    private InputStream createStreamForParsing(byte[] bytes, IJsonProgressChangeListener listener, ICancelled task) throws IllegalStateException, IOException{
        InputStream memoryStream = IoUtils.modifyInputStream(new ByteArrayInputStream(bytes), listener.getContentLength(), listener, task);
        
        return memoryStream;
    }
        
    private static class CancelTaskException extends Exception {
        private static final long serialVersionUID = -4423318670475901875L;
    }
    
    private static class NotJsonException extends Exception {
        private static final long serialVersionUID = -4423318670475901875L;
    }
}
