package cn.oyzh.fx.plus.property;


/**
 * 键值对属性
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class KeyValueProperty<K, V> {

    /**
     * 键
     */
    private K key;

    /**
     * 值
     */
    private V value;

    /**
     * 构造键值属性对象。
     */
    public KeyValueProperty() {
    }

    /**
     * 构造键值属性对象。
     *
     * @param key 键
     * @param value 值
     */
    public KeyValueProperty(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 获取键。
     *
     * @return 键
     */
    public K getKey() {
        return key;
    }

    /**
     * 设置键。
     *
     * @param key 键
     */
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * 获取值。
     *
     * @return 值
     */
    public V getValue() {
        return value;
    }

    /**
     * 设置值。
     *
     * @param value 值
     */
    public void setValue(V value) {
        this.value = value;
    }

    /**
     * 创建键值对属性
     *
     * @param key   键
     * @param value 值
     * @return 键值对属性
     */
    public static <K1, V1> KeyValueProperty<K1, V1> of(K1 key, V1 value) {
        return new KeyValueProperty<>(key, value);
    }
}
