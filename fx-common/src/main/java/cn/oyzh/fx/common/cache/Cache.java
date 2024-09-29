package cn.oyzh.fx.common.cache;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public abstract class Cache<K,V> {


    public abstract V get(K key) ;

    public abstract void put(K key,V value) ;

    public abstract void remove(K key) ;

    public abstract void clear() ;
}
