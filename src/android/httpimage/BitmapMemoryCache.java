package android.httpimage;

import android.graphics.Bitmap;

import com.vortexwolf.chan.common.library.LruCache;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.interfaces.ILruCacheListener;

public class BitmapMemoryCache implements BitmapCache {

    private final LruCache<String, Bitmap> mMap;

    public BitmapMemoryCache() {
        this.mMap = new LruCache<String, Bitmap>(new BitmapLruCacheListener());
    }

    @Override
    public synchronized boolean exists(String key) {
        return this.mMap.containsKey(key);
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
    
    private class BitmapLruCacheListener implements ILruCacheListener<String, Bitmap> {
        @Override
        public void onEntryRemoved(String key, Bitmap value) {
            MyLog.d("BitmapMemoryCache", "removed from memory " + key);
            // check if the bitmap is displayed
        }
    }
}
