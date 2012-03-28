package android.httpimage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.vortexwolf.dvach.common.http.GzipHttpClientFactory;
import com.vortexwolf.dvach.common.library.FlushedInputStream;
import com.vortexwolf.dvach.common.library.MyLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;


/**
 * resource loader using apache HTTP client. support HTTP and HTTPS request.
 * 
 * @author zonghai@gmail.com
 */
public class NetworkResourceLoader {
    public static final String TAG = "NetworkResourceLoader";

    private final HttpClient mHttpClient;

    public NetworkResourceLoader(){
    	this(GzipHttpClientFactory.createGzipHttpClient());
    }
    
    public NetworkResourceLoader(HttpClient httpClient){
    	mHttpClient = httpClient;
    }

    public Bitmap loadBitmap (Uri uri) {
    	HttpGet request = null;
		HttpEntity entity = null;
		Bitmap bmp = null;
		try {
			MyLog.d(TAG, "Image load started: " + uri);
			request = new HttpGet(uri.toString());
			HttpResponse response = this.mHttpClient.execute(request);
			entity = response.getEntity();
			FlushedInputStream fis = new FlushedInputStream(entity.getContent());
			bmp = BitmapFactory.decodeStream(fis);
	        MyLog.d(TAG, "Image downloaded: " + uri);
		} catch (Exception e) {
			MyLog.e(TAG, e);
		} finally {
			if (entity != null){
				try {
					entity.consumeContent();
				} catch (IOException e) {
					MyLog.e(TAG, e);
				}
			}
			if(request != null){
				request.abort();
			}
		}
		return bmp;
    }
}
