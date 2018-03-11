package httpimage;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.services.http.HttpBitmapReader;

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

    private static final String TAG = HttpImageManager.class.getSimpleName();

    private final BitmapMemoryCache mCache;
    private final FileSystemPersistence mPersistence;
    private final HttpBitmapReader mNetworkResourceLoader;
    private final Resources mResources;

    private final Handler mHandler = new Handler();
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(4);

    private final Set<LoadRequest> mActiveRequests = new HashSet<LoadRequest>();

    public static class LoadRequest {

        public LoadRequest(Uri uri) {
            this(uri, null);
        }

        public LoadRequest(Uri uri, OnLoadResponseListener l) {
            if (uri == null) {
                throw new NullPointerException("uri must not be null");
            }

            this.mUri = uri;
            this.mHashedUri = this.computeHashedName(uri.toString());
            this.mListener = l;
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
                byte[] hash = digest.digest();
                BigInteger bi = new BigInteger(1, hash);
                String hashtext = bi.toString(16);
                return hashtext;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private Uri mUri;
        private String mHashedUri;

        private OnLoadResponseListener mListener;
    }

    public static interface OnLoadResponseListener {
        public void beforeLoad(LoadRequest r);

        public void onLoadResponse(LoadRequest r, Bitmap data);

        public void onLoadError(LoadRequest r, Throwable e);
    }

    public HttpImageManager(BitmapMemoryCache cache, FileSystemPersistence persistence, Resources resources, HttpBitmapReader httpBitmapReader) {
        this.mCache = cache;
        this.mPersistence = persistence;
        this.mNetworkResourceLoader = httpBitmapReader;
        this.mResources = resources;
    }

    public Bitmap loadImage(Uri uri) {
        return this.loadImage(new LoadRequest(uri), false);
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
    public Bitmap loadImage(LoadRequest r, boolean reduceSize) {
        if (r == null || r.getUri() == null || TextUtils.isEmpty(r.getUri().toString())) {
            throw new IllegalArgumentException("null or empty request");
        }

        String key = r.getHashedUri();

        Bitmap cachedBitmap = this.mCache.loadData(key);
        if (cachedBitmap == null) {
            // not ready yet, try to retrieve it asynchronously.
            if (r.mListener != null) {
                r.mListener.beforeLoad(r);
            }

            this.mExecutor.submit(this.newRequestCall(r, reduceSize));

            return null;
        }

        return cachedBitmap;
    }

    // //PRIVATE
    private Callable<LoadRequest> newRequestCall(final LoadRequest request, final boolean reduceSize) {
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
                            MyLog.e(TAG, e);
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
                        data = HttpImageManager.this.mPersistence.loadData(key);
                        if (data != null) {
                            // load it into memory
                            if (reduceSize) {
                                data = AppearanceUtils.reduceBitmapSize(HttpImageManager.this.mResources, data);
                            }
                            HttpImageManager.this.mCache.storeData(key, data);
                        } else {
                            // we go to network
                            HttpImageManager.this.mNetworkResourceLoader.removeIfModifiedForUri(request.getUri().toString());
                            data = HttpImageManager.this.mNetworkResourceLoader.fromUri(request.getUri().toString());

                            // load it into memory
                            if (reduceSize) {
                                data = AppearanceUtils.reduceBitmapSize(HttpImageManager.this.mResources, data);
                            }
                            HttpImageManager.this.mCache.storeData(key, data);

                            // persist it
                            HttpImageManager.this.mPersistence.storeData(key, data);
                        }
                    }

                    if (data != null && request.mListener != null) {
                        final Bitmap theData = data;

                        HttpImageManager.this.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.mListener.onLoadResponse(request, theData);
                            }
                        });
                    }

                } catch (final Throwable e) {
                    if (request.mListener != null) {
                        HttpImageManager.this.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.mListener.onLoadError(request, e);
                            }
                        });
                    }
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
