package com.vortexwolf.dvach.interfaces;

public interface ILruCacheListener<K, V> {
    void onEntryRemoved(K key, V value);
}
