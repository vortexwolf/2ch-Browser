package com.vortexwolf.dvach.services.domain;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

import com.vortexwolf.chan.R;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.exceptions.HttpRequestException;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;

public class HttpStringReader implements IHttpStringReader {
    public static final String TAG = "HttpStringReader";

    private final HttpBytesReader mHttpBytesReader;

    public HttpStringReader(DefaultHttpClient httpClient, Resources resources) {
        this.mHttpBytesReader = new HttpBytesReader(httpClient, resources);
    }

    @Override
    public String fromUri(String uri) throws HttpRequestException {
        return this.fromUri(uri, null);
    }
    
    @Override
    public String fromUri(String uri, Header[] customHeaders) throws HttpRequestException {
        byte[] bytes = this.mHttpBytesReader.fromUri(uri, customHeaders);

        String result = IoUtils.convertBytesToString(bytes);
        return result;
    }

    public String fromResponse(HttpResponse response) throws NotFoundException, HttpRequestException {
        byte[] bytes = this.mHttpBytesReader.fromResponse(response);
        
        String result = IoUtils.convertBytesToString(bytes);
        return result;
    }
}
