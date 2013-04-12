package com.vortexwolf.dvach.common.library;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private static final int INITIAL_CAPACITY = 60;
    private static final float LOAD_FACTOR = 0.75F;
    public static final int MAX_CAPACITY = 120;

    public LruCache() {
        super(INITIAL_CAPACITY, LOAD_FACTOR, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> paramEntry) {
        if (this.size() <= MAX_CAPACITY) {
            return false;
        } else {
            return true;
        }
    }
}
