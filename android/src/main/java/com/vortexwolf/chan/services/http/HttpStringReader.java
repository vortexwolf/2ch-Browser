package com.vortexwolf.chan.services.http;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.IHttpStringReader;

public class HttpStringReader implements IHttpStringReader {
    public static final String TAG = "HttpStringReader";

    private final HttpBytesReader mHttpBytesReader;

    public HttpStringReader(HttpBytesReader httpBytesReader) {
        this.mHttpBytesReader = httpBytesReader;
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

    public String fromResponse(HttpResponse response) throws HttpRequestException {
        byte[] bytes = this.mHttpBytesReader.fromResponse(response);

        String result = IoUtils.convertBytesToString(bytes);
        return result;
    }
}
