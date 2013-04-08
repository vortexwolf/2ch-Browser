package com.vortexwolf.dvach.services.domain;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;

public class HttpStringReader implements IHttpStringReader {
    public static final String TAG = "HttpStringReader";

    private final DefaultHttpClient mHttpClient;

    public HttpStringReader(DefaultHttpClient httpClient) {
        this.mHttpClient = httpClient;
    }

    @Override
    public String fromUri(String uri, Header[] customHeaders) {
        HttpGet request = null;
        HttpResponse response = null;
        String result = null;

        try {
            request = new HttpGet(uri);
            if (customHeaders != null) {
                request.setHeaders(customHeaders);
            }

            response = this.mHttpClient.execute(request);

            result = this.fromResponse(response);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            ExtendedHttpClient.releaseRequestResponse(request, response);
        }

        return result;
    }

    @Override
    public String fromUri(String uri) {
        return this.fromUri(uri, null);
    }

    @Override
    public String fromResponse(HttpResponse response) {
        try {
            InputStream stream = response.getEntity().getContent();
            
            String result = IoUtils.convertStreamToString(stream);
            return result;
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            ExtendedHttpClient.releaseRequestResponse(null, response);
        }

        return null;
    }
}
