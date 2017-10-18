package ua.in.quireg.chan.services.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import android.content.res.Resources;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.library.ExtendedHttpClient;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.exceptions.HttpRequestException;
import ua.in.quireg.chan.interfaces.ICancelled;
import ua.in.quireg.chan.interfaces.IProgressChangeListener;

public class HttpStreamReader {
    public static final String TAG = "HttpStreamReader";

    private final DefaultHttpClient mHttpClient;
    private final Resources mResources;
    private final HashMap<String, String> mIfModifiedMap = new HashMap<String, String>();

    public HttpStreamReader(DefaultHttpClient httpClient, Resources resources) {
        mHttpClient = httpClient;
        mResources = resources;
    }

    public HttpStreamModel fromUri(String uri) throws HttpRequestException {
        return fromUri(uri, null);
    }

    public HttpStreamModel fromUri(String uri, Header[] customHeaders) throws HttpRequestException {
        return fromUri(uri, customHeaders, null, null);
    }

    public HttpStreamModel fromUri(String uri, Header[] customHeaders, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        return fromUri(uri, true, customHeaders, listener, task);
    }

    public HttpStreamModel fromUri(String uri, boolean checkModified, Header[] customHeaders, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        HttpGet request = null;
        HttpResponse response = null;
        InputStream stream = null;
        boolean wasNotModified = false;

        try {
            request = createPrivateRequest(uri, checkModified, customHeaders);
            response = getPrivateResponse(request);

            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 304) {
                wasNotModified = true;
            } else if (status.getStatusCode() != 200 && status.getStatusCode() != 403) {
                throw new HttpRequestException(status.getStatusCode() + " - " + status.getReasonPhrase());
            } else {
                stream = fromResponse(response, listener, task);
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
            request = createRequest(uri, headersList, entity);
            try {
                response = getResponse(request);
            } catch (SSLException e) {
                throw new HttpRequestException(mResources.getString(R.string.error_unsafe_ssl));
            } catch (Exception e) {
                MyLog.e(TAG, e.getMessage());
                throw new HttpRequestException(mResources.getString(R.string.error_download_data));
            }

            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200 && status.getStatusCode() != 403) {
                throw new HttpRequestException(status.getStatusCode() + " - " + status.getReasonPhrase());
            } else {
                stream = fromResponse(response, listener, task);
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
        return fromResponse(response, null, null);
    }

    public InputStream fromResponse(HttpResponse response, IProgressChangeListener listener, ICancelled task) throws HttpRequestException {
        try {
            HttpEntity entity = response.getEntity();

            InputStream stream = IoUtils.modifyInputStream(entity.getContent(), entity.getContentLength(), listener, task);

            return stream;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            ExtendedHttpClient.releaseResponse(response);
            throw new HttpRequestException(mResources.getString(R.string.error_read_response), e);
        }
    }

    public void removeIfModifiedForUri(String uri) {
        mIfModifiedMap.remove(uri);
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
                response = mHttpClient.execute(request);

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

    private HttpGet createPrivateRequest(String uri, boolean checkModified, Header[] customHeaders) throws HttpRequestException {
        List<Header> headersList = customHeaders != null ? Arrays.asList(customHeaders) : new ArrayList<Header>();

        if (checkModified && mIfModifiedMap.containsKey(uri)) {
            headersList.add(new BasicHeader("If-Modified-Since", mIfModifiedMap.get(uri)));
        }

        HttpGet request = createRequest(uri, headersList);
        if (request == null) {
            throw new HttpRequestException(mResources.getString(R.string.error_create_request));
        }

        return request;
    }

    private HttpResponse getPrivateResponse(HttpGet request) throws HttpRequestException {
        HttpResponse response = null;
        try {
            response = getResponse(request);
        } catch (SSLException e) {
            throw new HttpRequestException(mResources.getString(R.string.error_unsafe_ssl));
        } catch (Exception e) {
            MyLog.e(TAG, e.getMessage());
            throw new HttpRequestException(mResources.getString(R.string.error_download_data));
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            // save the last modified date
            Header header = response.getFirstHeader("Last-Modified");
            if (header != null) {
                mIfModifiedMap.put(request.getURI().toString(), header.getValue());
            }
        }

        return response;
    }
}
