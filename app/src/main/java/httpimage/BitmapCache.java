package httpimage;

import android.graphics.Bitmap;

/**
 * BitmapCache
 * 
 * @author zonghai@gmail.com
 */
public interface BitmapCache {

    /**
     * Retrieve the bitmap, return null means cache miss
     * 
     * @param key
     */
    public Bitmap loadData(String key);

    /**
     * Store bitmap
     * 
     * @param key
     * @param data
     */
    public void storeData(String key, Bitmap data);

    /**
     * Clear this bitmap cache, reclaim all resources assigned.
     */
    public void clear();
}
