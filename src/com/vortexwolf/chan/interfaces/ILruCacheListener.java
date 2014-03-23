package com.vortexwolf.chan.interfaces;

public interface ILruCacheListener<K, V> {
    void onEntryRemoved(K key, V value);
}
