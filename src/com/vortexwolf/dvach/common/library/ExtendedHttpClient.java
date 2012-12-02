package com.vortexwolf.dvach.common.library;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;

import com.vortexwolf.dvach.common.Constants;

public class ExtendedHttpClient extends DefaultHttpClient {

	private static final BasicHttpParams sParams;
	private static final ClientConnectionManager sConnectionManager;
	
	static {
		// HTTPS scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		SSLSocketFactory ssf = SSLSocketFactory.getSocketFactory();
		ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https", ssf, 443));
		
		// Client parameters
		sParams = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(sParams, false);
        ConnManagerParams.setTimeout(sParams, 15 * 1000);
        
        // Multi threaded connection manager
        sConnectionManager = new ThreadSafeClientConnManager(sParams, schemeRegistry);
		
	}
	
	public ExtendedHttpClient() {
		super(sConnectionManager, sParams);
		
		this.addRequestInterceptor(new DefaultRequestInterceptor());
		this.addResponseInterceptor(new GzipResponseInterceptor());
	}
	
	@Override
	public <T> T execute(HttpHost target, HttpRequest request,
			ResponseHandler<? extends T> responseHandler, HttpContext context)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return super.execute(target, request, responseHandler, context);
	}



	/** Adds default headers */
	private static class DefaultRequestInterceptor implements HttpRequestInterceptor {
        @Override
		public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            request.setHeader("User-Agent", Constants.USER_AGENT_STRING);
            if (!request.containsHeader("Accept-Encoding")){
                request.addHeader("Accept-Encoding", "gzip");
            }
        }
	}
	
	/** Handles responces with the gzip encoding */
	private static class GzipResponseInterceptor implements HttpResponseInterceptor {
		@Override
		public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity != null && entity.getContentEncoding() != null) {
                HeaderElement[] codecs = entity.getContentEncoding().getElements();
                for (int i = 0; i < codecs.length; i++) {
                    if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                        response.setEntity(new GzipDecompressingEntity(response.getEntity())); 
                        return;
                    }
                }
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
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }
        
        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }
	
}
