package android.httpimage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.interfaces.INetworkResourceLoader;

/**
 * HttpImageManager uses 3-level caching to download and store network images.
 * <p>
 * ---------------<br>
 * memory cache<br>
 * ---------------<br>
 * persistent storage (DB/FS)<br>
 * ---------------<br>
 * network loader<br>
 * ---------------
 * 
 * <p>
 * HttpImageManager will first look up the memory cache, return the image bitmap
 * if it was already cached in memory. Upon missing, it will further look at the
 * 2nd level cache, which is the persistence layer. It only goes to network if
 * the resource has never been downloaded.
 * 
 * <p>
 * The downloading process is handled in asynchronous manner. To get
 * notification of the response, one can add an OnLoadResponseListener to the
 * LoadRequest object.
 * 
 * <p>
 * HttpImageManager is usually used for ImageView to display a network image. To
 * simplify the code, One can register an ImageView object as target to the
 * LoadRequest instead of an OnLoadResponseListener. HttpImageManager will try
 * to feed the loaded resource to the target ImageView upon successful download.
 * Following code snippet shows how it is used in a customer list adapter.
 * 
 * <p>
 * 
 * <pre>
 *         ...
 *         String imageUrl = userInfo.getUserImage();
 *         ImageView imageView = holder.image;
 * 
 *         imageView.setImageResource(R.drawable.default_image);
 * 
 *         if(!TextUtils.isEmpty(imageUrl)){
 *             Bitmap bitmap = mHttpImageManager.loadImage(new HttpImageManager.LoadRequest(Uri.parse(imageUrl), imageView));
 *            if (bitmap != null) {
 *                imageView.setImageBitmap(bitmap);
 *            }
 *        }
 * 
 * </pre>
 * 
 * @author zonghai@gmail.com
 */
public class HttpImageManager {

    private static final String TAG = "HttpImageManager";

    private final BitmapCache mCache;
    private final BitmapCache mPersistence;
    private final INetworkResourceLoader mNetworkResourceLoader;

    private final Handler mHandler = new Handler();
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(4);

    private final Set<LoadRequest> mActiveRequests = new HashSet<LoadRequest>();

    public static class LoadRequest {

        public LoadRequest(Uri uri) {
            this(uri, null, null);
        }

        public LoadRequest(Uri uri, ImageView v) {
            this(uri, v, null);
        }

        public LoadRequest(Uri uri, OnLoadResponseListener l) {
            this(uri, null, l);
        }

        public LoadRequest(Uri uri, ImageView v, OnLoadResponseListener l) {
            if (uri == null) {
                throw new NullPointerException("uri must not be null");
            }

            this.mUri = uri;
            this.mHashedUri = this.computeHashedName(uri.toString());
            this.mImageView = v;
            this.mListener = l;
        }

        public ImageView getImageView() {
            return this.mImageView;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public String getHashedUri() {
            return this.mHashedUri;
        }

        @Override
        public int hashCode() {
            return this.mUri.hashCode();
        }

        @Override
        public boolean equals(Object b) {
            if (b instanceof LoadRequest) {
                return this.mUri.equals(((LoadRequest) b).getUri());
            }

            return false;
        }

        /* B64 encoded Hash over the input name */
        private String computeHashedName(String name) {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(name.getBytes());
                return Base64.encodeBytes(digest.digest()).replace("/", "_");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private Uri mUri;
        private String mHashedUri;

        private OnLoadResponseListener mListener;
        private ImageView mImageView;
    }

    public static interface OnLoadResponseListener {
        public void beforeLoad(LoadRequest r);

        public void onLoadResponse(LoadRequest r, Bitmap data);

        public void onLoadError(LoadRequest r, Throwable e);
    }

    public HttpImageManager(BitmapCache cache, BitmapCache persistence, DefaultHttpClient httpClient) {
        this.mCache = cache;
        this.mPersistence = persistence;
        this.mNetworkResourceLoader = new NetworkResourceLoader(httpClient);
    }

    public HttpImageManager(BitmapCache persistence, DefaultHttpClient httpClient) {
        this(new BasicBitmapCache(), persistence, httpClient);
    }

    public Bitmap loadImage(Uri uri) {
        return this.loadImage(new LoadRequest(uri));
    }

    public boolean isCached(String uriString) {
        LoadRequest r = new LoadRequest(Uri.parse(uriString));
        String key = r.getHashedUri();

        return this.mCache.exists(key);
    }

    /**
     * Nonblocking call, return null if the bitmap is not in cache.
     * 
     * @param r
     * @return
     */
    public Bitmap loadImage(LoadRequest r) {
        if (r == null || r.getUri() == null || TextUtils.isEmpty(r.getUri().toString())) {
            throw new IllegalArgumentException("null or empty request");
        }

        ImageView v = r.getImageView();
        if (v != null) {
            synchronized (v) {
                v.setTag(r.getUri()); // bind URI to the ImageView, to prevent
                                      // image write-back of earlier requests.
            }
        }

        String key = r.getHashedUri();

        Bitmap cachedBitmap = this.mCache.loadData(key);
        if (cachedBitmap == null) {
            // not ready yet, try to retrieve it asynchronously.
            if (r.mListener != null) {
                r.mListener.beforeLoad(r);
            }
            this.mExecutor.submit(this.newRequestCall(r));
            return null;
        }

        return cachedBitmap;
    }

    // //PRIVATE
    private Callable<LoadRequest> newRequestCall(final LoadRequest request) {
        return new Callable<LoadRequest>() {
            @Override
            public LoadRequest call() {

                synchronized (HttpImageManager.this.mActiveRequests) {
                    // If there's been already request pending for the same URL,
                    // we just wait until it is handled.
                    while (HttpImageManager.this.mActiveRequests.contains(request)) {
                        try {
                            HttpImageManager.this.mActiveRequests.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    HttpImageManager.this.mActiveRequests.add(request);
                }

                Bitmap data = null;

                try {
                    String key = request.getHashedUri();

                    // first we lookup memory cache
                    data = HttpImageManager.this.mCache.loadData(key);
                    if (data == null) {
                        // then check the persistent storage
                        data = HttpImageManager.this.mPersistence.isEnabled()
                                ? HttpImageManager.this.mPersistence.loadData(key)
                                : null;
                        if (data != null) {
                            // MyLog.d(TAG, "found in persistent: " +
                            // request.getUri().toString());
                            // load it into memory
                            HttpImageManager.this.mCache.storeData(key, data);
                        } else {
                            // we go to network
                            data = HttpImageManager.this.mNetworkResourceLoader.loadBitmap(request.getUri());
                            if (data == null) {
                                throw new RuntimeException("data from remote can't be decoded to bitmap");
                            }

                            // load it into memory
                            HttpImageManager.this.mCache.storeData(key, data);

                            // persist it
                            if (HttpImageManager.this.mPersistence.isEnabled()) {
                                HttpImageManager.this.mPersistence.storeData(key, data);
                            }
                        }
                    }

                    if (data != null) {
                        final Bitmap theData = data;
                        final ImageView iv = request.getImageView();

                        if (iv != null) {
                            HttpImageManager.this.mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (iv.getTag() == request.getUri()) {
                                        iv.setImageBitmap(theData);
                                    }
                                }
                            });
                        }

                        HttpImageManager.this.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (request.mListener != null) {
                                    request.mListener.onLoadResponse(request, theData);
                                }
                            }
                        });
                    }

                } catch (final Throwable e) {
                    HttpImageManager.this.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (request.mListener != null) {
                                request.mListener.onLoadError(request, e);
                            }
                        }
                    });
                    MyLog.e(TAG, e);
                } finally {
                    synchronized (HttpImageManager.this.mActiveRequests) {
                        HttpImageManager.this.mActiveRequests.remove(request);
                        HttpImageManager.this.mActiveRequests.notifyAll(); // wake up pending requests
                        // who's querying the same
                        // URL.
                    }
                }

                return request;
            }
        };
    }
}
