package com.vortexwolf.chan.services.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.Header;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.vortexwolf.chan.common.library.FlushedInputStream;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;

public class HttpBitmapReader {
    public static final String TAG = "HttpBitmapReader";

    private final HttpBytesReader mHttpBytesReader;

    public HttpBitmapReader(HttpBytesReader httpBytesReader) {
        this.mHttpBytesReader = httpBytesReader;
    }

    public Bitmap fromUri(String uri) throws HttpRequestException {
        return this.fromUri(uri, null);
    }

    public Bitmap fromUri(String uri, Header[] customHeaders) throws HttpRequestException {
        byte[] bytes = this.mHttpBytesReader.fromUri(uri, customHeaders);
        InputStream stream = new FlushedInputStream(new ByteArrayInputStream(bytes));

        Bitmap bmp = BitmapFactory.decodeStream(stream);

        IoUtils.closeStream(stream);

        return bmp;
    }

    public void removeIfModifiedForUri(String uri) {
        this.mHttpBytesReader.removeIfModifiedForUri(uri);
    }
}
