package com.vortexwolf.chan.services.domain;

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
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.IHttpStringReader;

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
