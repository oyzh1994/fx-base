package cn.oyzh.fx.plus;


import javafx.util.StringConverter;

/**
 * 字符串转换简单实现
 *
 * @author oyzh
 * @since 2022/8/23
 */
public class SimpleStringConverter<T> extends StringConverter<T> {

    @Override
    public String toString(T o) {
        return null;
    }

    @Override
    public T fromString(String s) {
        return null;
    }
}
