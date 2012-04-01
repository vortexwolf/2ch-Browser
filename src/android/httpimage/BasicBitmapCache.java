package android.httpimage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.vortexwolf.dvach.common.library.LruCache;
import com.vortexwolf.dvach.common.library.MyLog;

import android.graphics.Bitmap;
import android.util.Log;

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
