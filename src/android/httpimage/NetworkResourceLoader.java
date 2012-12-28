package android.httpimage;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.FlushedInputStream;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.interfaces.INetworkResourceLoader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

/**
 * resource loader using apache HTTP client. support HTTP and HTTPS request.
 * 
 * @author zonghai@gmail.com
 */
public class NetworkResourceLoader implements INetworkResourceLoader {
    public static final String TAG = "NetworkResourceLoader";

    private final HttpClient mHttpClient;

    public NetworkResourceLoader(HttpClient httpClient) {
        mHttpClient = httpClient;
    }

    @Override
    public Bitmap loadBitmap(Uri uri) {
        HttpGet request = null;
        HttpResponse response = null;
        Bitmap bmp = null;
        try {
            MyLog.d(TAG, "Image load started: " + uri);

            request = new HttpGet(uri.toString());
            response = this.mHttpClient.execute(request);
            HttpEntity entity = response.getEntity();
            FlushedInputStream fis = new FlushedInputStream(entity.getContent());
            bmp = BitmapFactory.decodeStream(fis);

            MyLog.d(TAG, "Image downloaded: " + uri);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            ExtendedHttpClient.releaseRequestResponse(request, response);
        }

        return bmp;
    }
}
