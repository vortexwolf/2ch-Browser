package com.vortexwolf.chan.services.domain;

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
import android.content.res.Resources.NotFoundException;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.library.CancellableInputStream;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.library.ProgressInputStream;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IProgressChangeListener;
import com.vortexwolf.chan.models.domain.HttpStreamModel;

public class HttpBytesReader {
    public static final String TAG = "HttpBytesReader";

    private final Resources mResources;
    private final HttpStreamReader mHttpStreamReader;

    public HttpBytesReader(DefaultHttpClient httpClient, Resources resources) {
        this.mHttpStreamReader = new HttpStreamReader(httpClient, resources);
        this.mResources = resources;
    }

    public byte[] fromUri(String uri) throws HttpRequestException {
        return this.fromUri(uri, null);
    }
    
    public byte[] fromUri(String uri, Header[] customHeaders) throws HttpRequestException {
        return this.fromUri(uri, customHeaders, null, null);
    }
    
    public byte[] fromUri(String uri, Header[] customHeaders, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        HttpStreamModel streamModel = this.mHttpStreamReader.fromUri(uri, customHeaders, listener, task);
        
        try {
            byte[] result = this.convertStreamToBytes(streamModel.stream);        
            return result;
        } finally {
            ExtendedHttpClient.releaseRequestResponse(streamModel.request, streamModel.response);
        }
    }

    public byte[] fromResponse(HttpResponse response) throws HttpRequestException {
        return this.fromResponse(response, null, null);
    }
    
    public byte[] fromResponse(HttpResponse response, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        InputStream stream = this.mHttpStreamReader.fromResponse(response, listener, task);
        
        try {
            byte[] result = this.convertStreamToBytes(stream);        
            return result;
        } finally {
            ExtendedHttpClient.releaseResponse(response);
        }
    }
    
    public void removeIfModifiedForUri(String uri) {
        this.mHttpStreamReader.removeIfModifiedForUri(uri);
    }
    
    private byte[] convertStreamToBytes(InputStream stream) throws HttpRequestException{
        try {
            byte[] result = IoUtils.convertStreamToBytes(stream);
            return result;
        } catch (Exception e) {
            throw new HttpRequestException(this.mResources.getString(R.string.error_read_response), e);
        }
    }
}
