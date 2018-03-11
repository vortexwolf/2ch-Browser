package httpimage;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.in.quireg.chan.interfaces.ILruCacheListener;

@SuppressWarnings("serial")
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private static final int MAX_CAPACITY = 50;

    private final ILruCacheListener<K, V> mListener;

    LruCache(ILruCacheListener<K, V> listener) {
        super(16, 1, true);

        mListener = listener;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
        if (size() <= MAX_CAPACITY) {
            return false;
        } else {
            if (mListener != null) {
                mListener.onEntryRemoved(entry.getKey(), entry.getValue());
            }
            return true;
        }
    }
}
