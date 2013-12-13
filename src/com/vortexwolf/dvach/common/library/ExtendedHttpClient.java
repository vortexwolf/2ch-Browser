package com.vortexwolf.dvach.common.library;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import com.vortexwolf.dvach.common.Constants;

public class ExtendedHttpClient extends DefaultHttpClient {
    private static final String TAG = "ExtendedHttpClient";

    private static final int SOCKET_OPERATION_TIMEOUT = 15 * 1000;

    private static final BasicHttpParams sParams;
    private static final ClientConnectionManager sConnectionManager;

    static {

        // Client parameters
        BasicHttpParams params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_OPERATION_TIMEOUT)
                .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, SOCKET_OPERATION_TIMEOUT)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);

        ConnManagerParams.setTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpProtocolParams.setUserAgent(params, Constants.USER_AGENT_STRING);
        
        // HTTPS scheme registry
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        SSLSocketFactory ssf = SSLSocketFactory.getSocketFactory();
        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        schemeRegistry.register(new Scheme("https", ssf, 443));

        // Multi threaded connection manager
        sParams = params;
        sConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);

    }

    public ExtendedHttpClient() {
        super(sConnectionManager, sParams);

        //System.setProperty("http.KeepAlive","false");
        
        this.addRequestInterceptor(new DefaultRequestInterceptor());
        this.addResponseInterceptor(new GzipResponseInterceptor());
    }

    /** Releases all resources of the request and response objects */
    public static void releaseRequestResponse(HttpRequestBase request, HttpResponse response) {
        releaseResponse(response);

        releaseRequest(request);
    }
    
    public static void releaseRequest(HttpRequestBase request) {
        if (request != null) {
            request.abort();
        }
    }
    
    public static void releaseResponse(HttpResponse response) {
        if (response == null) {
            return;
        }
        
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                entity.consumeContent();
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }
    }

    public static String getLocationHeader(HttpResponse response) {
    	if (response == null){
            return null;
    	}
    	
        Header header = response.getFirstHeader("Location");
        if (header != null) {
            return header.getValue();
        }

        return null;
    }

    /** Adds default headers */
    private static class DefaultRequestInterceptor implements HttpRequestInterceptor {
        @Override
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            request.addHeader("Accept-Encoding", "gzip");
        }
    }

    /** Handles responces with the gzip encoding */
    private static class GzipResponseInterceptor implements HttpResponseInterceptor {
        @Override
        public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return;
            }
            Header header = entity.getContentEncoding();
            if (header == null) {
                return;
            }
            String contentEncoding = header.getValue();
            if (contentEncoding == null) {
                return;
            }

            if (contentEncoding.contains("gzip")) {
                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
            }
        }
    }

    private static class GzipDecompressingEntity extends HttpEntityWrapper {
        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = this.wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }

}
