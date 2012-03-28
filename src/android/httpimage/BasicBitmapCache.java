package android.httpimage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.vortexwolf.dvach.common.library.MyLog;

import android.graphics.Bitmap;
import android.util.Log;


/**
 * Basic implementation of BitmapCache 
 * 
 * @author zonghai@gmail.com
 */
public class BasicBitmapCache implements BitmapCache{
    
    private static class CacheEntry {
    	public String url;
        public Bitmap data;
        public int nUsed;
        public long timestamp;
    }
    
    
    private static final String TAG = "BasicBitmapCache";

    private final int mMaxSize;
    private HashMap<String, CacheEntry> mMap = new HashMap<String, CacheEntry> ();
    private SortedSet<CacheEntry> mSet = new TreeSet<CacheEntry>(new CacheEntryComparator());

    /**
     * max number of resource this cache contains
     * @param size
     */
    public BasicBitmapCache (int size) {
        this.mMaxSize = size;
    }
    
    
    @Override
    public synchronized boolean exists(String key){
       return mMap.get(key) != null;
    }

    
    @Override
    public synchronized void invalidate(String key){
        CacheEntry e = mMap.get(key);
        invalidate(e);
    }
    
    public synchronized void invalidate(CacheEntry e){
        mMap.remove(e.url);
        mSet.remove(e);
    }
    
    @Override
    public synchronized void clear(){
         for ( String key : mMap.keySet()) {
             invalidate(key);
         }
    }

    
    /**
     * If the cache storage is full, return an item to be removed. 
     * 
     * Default strategy: the least and oldest out: O(n)
     * 
     * @return item key
     */
    protected synchronized String findItemToInvalidate() {
        Map.Entry<String, CacheEntry> out = null;
        for(Map.Entry<String, CacheEntry> e : mMap.entrySet()){
            if( out == null || e.getValue().nUsed < out.getValue().nUsed 
                    || e.getValue().nUsed == out.getValue().nUsed && e.getValue().timestamp < out.getValue().timestamp) {
                out = e;
            }
        }
        return out.getKey();
    }

    
    @Override
    public synchronized Bitmap loadData(String key) {
        if(!exists(key)) {
            return null;
        }
        CacheEntry res = mMap.get(key);
        res.nUsed++;
        res.timestamp = System.currentTimeMillis();
        return res.data;
    }


    @Override
    public synchronized void storeData(String key, Bitmap data) {
        if(this.exists(key)) {
            return;
        }
        CacheEntry res = new CacheEntry();
        res.url = key;
        res.nUsed = 1;
        res.timestamp = System.currentTimeMillis();
        res.data = data;
        
        //if the number exceeds, move an item out 
        //to prevent the storage from increasing indefinitely.
        if(mMap.size() >= mMaxSize) {
        	MyLog.d(TAG, "started to invalidate");
        	
        	ArrayList<CacheEntry> toInvalidate = new ArrayList<CacheEntry>();
        	int i = 0;
        	for(CacheEntry e : mSet){
        		toInvalidate.add(e);
        		if(i > mMaxSize / 2) break;
        		i++;
        	}
        	
        	for(CacheEntry e : toInvalidate){
        		this.invalidate(e);
        	}
        	
        	MyLog.d(TAG, "invalidated");
        }
        
        mMap.put(key, res);
        mSet.add(res);
    }
    
    private class CacheEntryComparator implements Comparator<CacheEntry>{
		@Override
		public int compare(CacheEntry left, CacheEntry right) {
			if(left.timestamp < right.timestamp){
				return -1;
			}
			else if (left.timestamp > right.timestamp){
				return 1;
			}
			
			return 0;
		}
    }

}
