package cn.oyzh.fx.plus.converter;


import javafx.util.StringConverter;

/**
 * 字符串转换简单实现
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class SimpleStringConverter<T> extends StringConverter<T> {

    @Override
    public String toString(T o) {
        return o.toString();
    }

    @Override
    public T fromString(String s) {
        return null;
    }
}
