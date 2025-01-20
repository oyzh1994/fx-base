package cn.oyzh.fx.plus.property;

import lombok.Data;

@Data
public class KeyValueProperty<K, V> {

    private K key;

    private V value;

    public KeyValueProperty() {
    }

    public KeyValueProperty(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K1, V1> KeyValueProperty<K1, V1> of(K1 key, V1 value) {
        return new KeyValueProperty<>(key, value);
    }
}
