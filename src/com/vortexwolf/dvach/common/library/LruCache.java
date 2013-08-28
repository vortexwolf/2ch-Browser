package com.vortexwolf.dvach.common.library;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vortexwolf.dvach.interfaces.ILruCacheListener;

@SuppressWarnings("serial")
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private static final int INITIAL_CAPACITY = 60;
    private static final float LOAD_FACTOR = 0.75F;
    public static final int MAX_CAPACITY = 120;
    
    private final ILruCacheListener<K, V> mListener;

    public LruCache(ILruCacheListener<K, V> listener) {
        super(INITIAL_CAPACITY, LOAD_FACTOR, true);
        
        this.mListener = listener;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
        if (this.size() <= MAX_CAPACITY) {
            return false;
        } else {
            if (this.mListener != null) {
                this.mListener.onEntryRemoved(entry.getKey(), entry.getValue());
            }
            
            return true;
        }
    }
}
