package cn.oyzh.fx.plus.converter;

import cn.oyzh.fx.plus.format.DigitalFormat;
import javafx.util.converter.FormatStringConverter;

/**
 * 数字格式化转换器
 *
 * @author oyzh
 * @since 2024-05-15
 */
public class DigitalConverter extends FormatStringConverter<String> {

    /**
     * 构建数字格式化转换器
     */
    public DigitalConverter() {
        this(new DigitalFormat());
    }

    /**
     * 构建数字格式化转换器
     *
     * @param format 数字格式
     */
    public DigitalConverter( DigitalFormat format) {
        super(format);
    }

    @Override
    public String fromString(String value) {
        return value;
    }

    @Override
    public String toString(String value) {
        return this.getFormat().format(value);
    }

    @Override
    protected DigitalFormat getFormat() {
        return (DigitalFormat) super.getFormat();
    }
}
