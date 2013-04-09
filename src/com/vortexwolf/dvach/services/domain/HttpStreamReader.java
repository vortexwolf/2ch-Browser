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

import android.content.res.Resources;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.library.CancellableInputStream;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.library.ProgressInputStream;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.exceptions.HttpRequestException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.models.domain.HttpStreamModel;

public class HttpStreamReader {
    public static final String TAG = "HttpStreamReader";
    
    private final DefaultHttpClient mHttpClient;
    private final Resources mResources;
    private final HashMap<String, String> mIfModifiedMap = new HashMap<String, String>();

    public HttpStreamReader(DefaultHttpClient httpClient, Resources resources) {
        this.mHttpClient = httpClient;
        this.mResources = resources;
    }

    public HttpStreamModel fromUri(String uri) throws HttpRequestException {
        return this.fromUri(uri, null);
    }
    
    public HttpStreamModel fromUri(String uri, Header[] customHeaders) throws HttpRequestException {
        return this.fromUri(uri, customHeaders, null, null);
    }
    
    public HttpStreamModel fromUri(String uri, Header[] customHeaders, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        HttpGet request = null;
        HttpResponse response = null;
        InputStream stream = null;

        try {
            request = this.createRequest(uri, customHeaders);
            response = this.getResponse(request);
            
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 304) {
                stream = null;
            }
            else if (status.getStatusCode() != 200) {
                throw new HttpRequestException(status.getStatusCode() + " - " + status.getReasonPhrase());
            }
            else {
                stream = this.fromResponse(response, listener, task);
            }
        
        } catch (HttpRequestException e) {
            ExtendedHttpClient.releaseRequestResponse(request, response);
            throw e;
        }
        
        HttpStreamModel result = new HttpStreamModel();
        result.stream = stream;
        result.request = request;
        result.response = response;
        
        return result;
    }

    public InputStream fromResponse(HttpResponse response) throws HttpRequestException {
        return this.fromResponse(response, null, null);
    }
    
    public InputStream fromResponse(HttpResponse response, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        try {
            HttpEntity entity = response.getEntity();
            
            InputStream stream = IoUtils.modifyInputStream(entity.getContent(), entity.getContentLength(), listener, task);
            
            return stream;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            ExtendedHttpClient.releaseResponse(response);
            throw new HttpRequestException(this.mResources.getString(R.string.error_read_response), e);
        }
    }
    
    public void removeIfModifiedForUri(String uri) {
        this.mIfModifiedMap.remove(uri);
    }
    
    private HttpGet createRequest(String uri, Header[] customHeaders) throws HttpRequestException {
        HttpGet request = null;
        try {
            request = new HttpGet(uri);
            
            if (this.mIfModifiedMap.containsKey(uri)) {
                request.setHeader("If-Modified-Since", this.mIfModifiedMap.get(uri));
            }
            
            if (customHeaders != null) {
                request.setHeaders(customHeaders);
            }

            return request;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            ExtendedHttpClient.releaseRequest(request);
            throw new HttpRequestException(this.mResources.getString(R.string.error_create_request), e);
        }
    }
    
    private HttpResponse getResponse(HttpGet request) throws HttpRequestException {
        HttpResponse response = null;
        try {
            response = this.mHttpClient.execute(request);

            if(response.getStatusLine().getStatusCode() == 200) {
                // save the last modified date
                Header header = response.getFirstHeader("Last-Modified");
                if (header != null) {
                    this.mIfModifiedMap.put(request.getURI().toString(), header.getValue());
                }
            }
            
            return response;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            ExtendedHttpClient.releaseRequestResponse(request, response);
            throw new HttpRequestException(this.mResources.getString(R.string.error_download_data), e);
        }
    }
}
