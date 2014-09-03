package com.vortexwolf.chan.services.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import android.content.res.Resources;
import android.net.Uri;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IProgressChangeListener;
import com.vortexwolf.chan.settings.ApplicationSettings;

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
        boolean wasNotModified = false;

        try {
            request = this.createPrivateRequest(uri, customHeaders);
            response = this.getPrivateResponse(request);

            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 304) {
                wasNotModified = true;
            } else if (status.getStatusCode() != 200 && status.getStatusCode() != 403) {
                throw new HttpRequestException(status.getStatusCode() + " - " + status.getReasonPhrase());
            } else {
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
        result.notModifiedResult = wasNotModified;

        return result;
    }
    
    public HttpStreamModel fromUri(String uri, Header[] customHeaders, HttpEntity entity, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
    	List<Header> headersList = customHeaders != null ? Arrays.asList(customHeaders) : new ArrayList<Header>();
        HttpPost request = null;
        HttpResponse response = null;
        InputStream stream = null;
        
        try {
        	request = this.createRequest(uri, headersList, entity);
        	try {
        		response = this.getResponse(request);
        	} catch (Exception e) {
                throw new HttpRequestException(this.mResources.getString(R.string.error_download_data));
            }
        	
        	StatusLine status = response.getStatusLine();
        	if (status.getStatusCode() != 200 && status.getStatusCode() != 403) {
        		throw new HttpRequestException(status.getStatusCode() + " - " + status.getReasonPhrase());
        	} else {
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
        result.notModifiedResult = false;

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

    public HttpGet createRequest(String uri, List<Header> customHeaders) throws IllegalArgumentException {
        HttpGet request = null;
        try {
            request = new HttpGet(uri);
            if (customHeaders != null) {
                for (Header header : customHeaders) {
                    request.addHeader(header);
                }
            }
        } catch (IllegalArgumentException e) {
            MyLog.e(TAG, e);
            ExtendedHttpClient.releaseRequest(request);
            throw e;
        }

        return request;
    }
    
    public HttpPost createRequest(String uri, List<Header> customHeaders, HttpEntity entity) throws IllegalArgumentException {
        HttpPost request = null;
        try {
            request = new HttpPost(uri);
            request.setHeader("content-type", "multipart/form-data; boundary=" + Constants.MULTIPART_BOUNDARY);
            if (customHeaders != null) {
                for (Header header : customHeaders) {
                    request.addHeader(header);
                }
            }
            request.setEntity(entity);
        } catch (IllegalArgumentException e) {
            MyLog.e(TAG, e);
            ExtendedHttpClient.releaseRequest(request);
            throw e;
        }
        return request;
    }
    
    public HttpResponse getResponse(HttpRequestBase request) throws IOException {
        HttpResponse response = null;
        IOException responseException = null;

        // try several times if exception, break the loop after a successful read
        for (int i = 0; i < 3; i++) {
            try {
                response = this.mHttpClient.execute(request);

                responseException = null;
                break;
            } catch (IOException e) {
                MyLog.e(TAG, e);
                responseException = e;

                if ("recvfrom failed: ECONNRESET (Connection reset by peer)".equals(e.getMessage())) {
                    // a stupid error, I have no idea how to solve it so I just try again
                    continue;
                } else {
                    break;
                }
            }
        }

        if (responseException != null) {
            //ExtendedHttpClient.releaseRequestResponse(request, response);
            throw responseException;
        }

        return response;
    }

    private HttpGet createPrivateRequest(String uri, Header[] customHeaders) throws HttpRequestException {
        List<Header> headersList = customHeaders != null ? Arrays.asList(customHeaders) : new ArrayList<Header>();

        if (this.mIfModifiedMap.containsKey(uri)) {
            headersList.add(new BasicHeader("If-Modified-Since", this.mIfModifiedMap.get(uri)));
        }

        HttpGet request = this.createRequest(uri, headersList);
        if (request == null) {
            throw new HttpRequestException(this.mResources.getString(R.string.error_create_request));
        }

        return request;
    }

    private HttpResponse getPrivateResponse(HttpGet request) throws HttpRequestException {
        HttpResponse response = null;
        try {
            response = this.getResponse(request);
        } catch (Exception e) {
            throw new HttpRequestException(this.mResources.getString(R.string.error_download_data));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            // save the last modified date
            Header header = response.getFirstHeader("Last-Modified");
            if (header != null) {
                this.mIfModifiedMap.put(request.getURI().toString(), header.getValue());
            }
        }

        return response;
    }
}
