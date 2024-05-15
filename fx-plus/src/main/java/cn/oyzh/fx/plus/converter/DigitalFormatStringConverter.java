package cn.oyzh.fx.plus.converter;

import cn.oyzh.fx.plus.format.DigitalDecimalFormat;
import javafx.util.converter.FormatStringConverter;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2024/5/15
 */
public class DigitalFormatStringConverter extends FormatStringConverter<String> {

    public DigitalFormatStringConverter() {
        this(new DigitalDecimalFormat());
    }

    public DigitalFormatStringConverter(@NonNull DigitalDecimalFormat format) {
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
    protected DigitalDecimalFormat getFormat() {
        return (DigitalDecimalFormat) super.getFormat();
    }
}
