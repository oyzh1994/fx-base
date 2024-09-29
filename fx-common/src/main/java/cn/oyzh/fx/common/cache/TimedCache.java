package cn.oyzh.fx.common.cache;

import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class TimedCache<K, V>  extends Cache<K,V>{

    private Map<K,V> cache;

    private Integer timeout;

    public TimedCache(int timeout) {
        this.timeout = timeout;
    }

    public TimedCache( ) {
    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public void remove(K key) {

    }

    @Override
    public void clear() {

    }
}
