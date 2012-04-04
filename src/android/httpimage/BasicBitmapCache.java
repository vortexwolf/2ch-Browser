package android.httpimage;

import com.vortexwolf.dvach.common.library.LruCache;
import android.graphics.Bitmap;

public class BasicBitmapCache implements BitmapCache{

	private final LruCache<String, Bitmap> mMap = new LruCache<String, Bitmap> ();

    public BasicBitmapCache () {
    
    }
    
    
    @Override
    public synchronized boolean exists(String key){
       return mMap.containsKey(key);
    }

    
    @Override
    public synchronized void invalidate(String key){
    	mMap.remove(key);
    }

    @Override
    public synchronized void clear(){
    	mMap.clear();
    }

    
    @Override
    public synchronized Bitmap loadData(String key) {      
        Bitmap res = mMap.get(key);

        return res;
    }


    @Override
    public synchronized void storeData(String key, Bitmap data) {
        if(this.exists(key)) {
            return;
        }

        mMap.put(key, data);
    }
}
