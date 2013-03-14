package android.httpimage;

import android.graphics.Bitmap;

import com.vortexwolf.dvach.common.library.LruCache;

public class BasicBitmapCache implements BitmapCache {

    private final LruCache<String, Bitmap> mMap = new LruCache<String, Bitmap>();

    public BasicBitmapCache() {

    }

    @Override
    public synchronized boolean exists(String key) {
        return this.mMap.containsKey(key);
    }

    @Override
    public synchronized void invalidate(String key) {
        this.mMap.remove(key);
    }

    @Override
    public synchronized void clear() {
        this.mMap.clear();
    }

    @Override
    public synchronized Bitmap loadData(String key) {
        Bitmap res = this.mMap.get(key);

        return res;
    }

    @Override
    public synchronized void storeData(String key, Bitmap data) {
        if (this.exists(key)) {
            return;
        }

        this.mMap.put(key, data);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
