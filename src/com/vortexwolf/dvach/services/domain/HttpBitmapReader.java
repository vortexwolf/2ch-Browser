package com.vortexwolf.dvach.services.domain;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.vortexwolf.dvach.common.library.FlushedInputStream;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.exceptions.HttpRequestException;

public class HttpBitmapReader {
    public static final String TAG = "HttpBitmapReader";

    private final HttpBytesReader mHttpBytesReader;

    public HttpBitmapReader(DefaultHttpClient httpClient, Resources resources) {
        this.mHttpBytesReader = new HttpBytesReader(httpClient, resources);
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
