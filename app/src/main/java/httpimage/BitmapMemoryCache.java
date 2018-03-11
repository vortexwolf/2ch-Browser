package httpimage;

import android.graphics.Bitmap;

import timber.log.Timber;

public class BitmapMemoryCache implements BitmapCache {

    private final LruCache<String, Bitmap> mMap;

    public BitmapMemoryCache() {
        mMap = new LruCache<>((key, value) -> {
            Timber.d("removed from memory %s", key);
        });
    }

    public synchronized boolean exists(String key) {
        return this.mMap.containsKey(key);
    }

    @Override
    public synchronized void clear() {
        mMap.clear();
    }

    @Override
    public synchronized Bitmap loadData(String key) {
        return mMap.get(key);
    }

    @Override
    public synchronized void storeData(String key, Bitmap data) {
        if (mMap.containsKey(key)) {
            return;
        }
        mMap.put(key, data);
    }

}
