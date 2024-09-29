package cn.oyzh.fx.common.cache;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class WeakCache<K, V> extends Cache<K, V> {

    private final Map<K, WeakReference<V>> cache;

    public WeakCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public V get(K key) {
        WeakReference<V> ref = cache.get(key);
        if (ref == null) {
            return null;
        }
        if (ref.get() == null) {
            cache.remove(key);
            return null;
        }
        return ref.get();
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, new WeakReference<>(value));
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public boolean containsKey(K key) {
        WeakReference<V> ref = cache.get(key);
        return ref != null && ref.get() != null;
    }
}
