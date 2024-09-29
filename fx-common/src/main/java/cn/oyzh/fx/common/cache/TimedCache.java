package cn.oyzh.fx.common.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class TimedCache<K, V> extends Cache<K, V> {

    private static class TimedValue<V> {
        private V value;
        private Long putTime;
    }

    private final long timeout;

    private final Map<K, TimedValue<V>> cache;

    public TimedCache(long timeout) {
        this.timeout = timeout;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public V get(K key) {
        TimedValue<V> value = this.cache.get(key);
        if (value == null) {
            return null;
        }
        // 检查值
        if (this.checkTimeout(value)) {
            this.cache.remove(key);
            return null;
        }
        return value.value;
    }

    @Override
    public void put(K key, V value) {
        TimedValue<V> timedValue = new TimedValue<>();
        timedValue.value = value;
        if (this.timeout > 0) {
            timedValue.putTime = System.currentTimeMillis();
        }
        this.cache.put(key, timedValue);
    }

    @Override
    public void remove(K key) {
        this.cache.remove(key);
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public boolean containsKey(K key) {
        TimedValue<V> value = this.cache.get(key);
        if (value == null) {
            return false;
        }
        return !this.checkTimeout(value);
    }

    private boolean checkTimeout(TimedValue<V> value) {
        if (value != null && this.timeout > 0) {
            return System.currentTimeMillis() - value.putTime > this.timeout;
        }
        return false;
    }
}
