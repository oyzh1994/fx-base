package cn.oyzh.fx.plus.property;


/**
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class KeyValueProperty<K, V> {

    private K key;

    private V value;

    public KeyValueProperty() {
    }

    public KeyValueProperty(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public static <K1, V1> KeyValueProperty<K1, V1> of(K1 key, V1 value) {
        return new KeyValueProperty<>(key, value);
    }
}
