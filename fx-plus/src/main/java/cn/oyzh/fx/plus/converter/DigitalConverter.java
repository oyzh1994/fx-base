package cn.oyzh.fx.plus.converter;

import cn.oyzh.fx.plus.format.DigitalFormat;
import javafx.util.converter.FormatStringConverter;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2024/5/15
 */
public class DigitalConverter extends FormatStringConverter<String> {

    public DigitalConverter() {
        this(new DigitalFormat());
    }

    public DigitalConverter(@NonNull DigitalFormat format) {
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
