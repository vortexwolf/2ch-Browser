package com.vortexwolf.dvach.common.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
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
public class GzipHttpClientFactory {
	
//	private static final DefaultHttpClient mGzipHttpClient = createGzipHttpClient();
//	
//	/**
//	 * http://hc.apache.org/httpcomponents-client/examples.html
//	 * @return a Gzip-enabled DefaultHttpClient
//	 */

	public DefaultHttpClient createHttpClient()
	{
		return createGzipHttpClient();
	}
	
	public static DefaultHttpClient createGzipHttpClient() {
		BasicHttpParams params = new BasicHttpParams();
		
		// Scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		SSLSocketFactory ssf = SSLSocketFactory.getSocketFactory();
		ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https", ssf, 443));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        
        // Default connection and socket timeout of 30 seconds. Tweak to taste.
        HttpConnectionParams.setConnectionTimeout(params, 10*1000);
        HttpConnectionParams.setSoTimeout(params, 20*1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        
        //ConnManagerParams.setTimeout(params, 5 * 1000);
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(50));
        ConnManagerParams.setMaxTotalConnections(params, 200);

        // http client
		DefaultHttpClient httpclient = new DefaultHttpClient(cm, params);
		
        httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
			public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                request.setHeader("User-Agent", Constants.USER_AGENT_STRING);
                if (!request.containsHeader("Accept-Encoding")){
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        });
        
        httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
			public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                	//header.getValue().contains("gzip")
                    HeaderElement[] codecs = ceheader.getElements();
                    for (int i = 0; i < codecs.length; i++) {
                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity())); 
                            return;
                        }
                    }
                }
            }
        });
        return httpclient;
	}
	
	static class GzipDecompressingEntity extends HttpEntityWrapper {
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
