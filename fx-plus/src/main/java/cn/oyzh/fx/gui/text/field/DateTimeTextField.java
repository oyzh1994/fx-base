package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.skin.DateTimeTextFieldSkin;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

/**
 * 日期时间文本输入框
 *
 * @author oyzh
 * @since 2024-07-19
 */
public class DateTimeTextField extends LimitTextField {

    /**
     * 默认日期时间格式
     */
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期时间格式（不含秒）
     */
    public static final SimpleDateFormat FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * ISO 日期时间格式
     */
    public static final SimpleDateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * ISO 日期时间格式（不含秒）
     */
    public static final SimpleDateFormat FORMAT_T_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    /**
     * 自定义日期格式
     */
    private SimpleDateFormat dateFormat;

    /**
     * 默认展示格式，按需追加小数秒
     */
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    private static final DateTimeFormatter FRACTION_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .toFormatter();

    /**
     * 非标准日期时间格式
     */
    private static final List<DateTimeFormatter> OTHER_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("uuuu-M-d H:mm:ss"),
            DateTimeFormatter.ofPattern("uuuu-M-d H:mm"),
            DateTimeFormatter.ofPattern("uuuu-M-d"),
            DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("uuuu/MM/dd"),
            DateTimeFormatter.ofPattern("uuuu/M/d"),
            DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm"),
            DateTimeFormatter.ofPattern("uuuu.MM.dd"),
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss"),
            DateTimeFormatter.ofPattern("uuuuMMdd")
    );

    /**
     * 获取自定义日期格式
     *
     * @return 自定义日期格式
     */
    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * 设置自定义日期格式
     *
     * @param dateFormat 自定义日期格式
     */
    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
        if (dateFormat != null) {
            this.skin().setFormatter(DateTimeFormatter.ofPattern(dateFormat.toPattern()));
        } else {
            this.skin().setFormatter(null);
        }
    }

    /**
     * 获取时间戳
     *
     * @return 时间戳，内容为空返回 null
     * @throws ParseException 文本解析失败时抛出
     */
    public Timestamp getTimestamp() throws ParseException {
        if (this.isEmpty() || isCurrentTimestamp(this.getText())) {
            return null;
        }
        LocalDateTime dateTime = parseDateTime(this.getText(), this.getDateFormat());
        if (dateTime == null) {
            throw new ParseException("Unparseable date: \"" + this.getText() + "\"", 0);
        }
        return Timestamp.valueOf(dateTime);
    }

    @Override
    public Object getValue() {
        String text = this.getText();
        if (isCurrentTimestamp(text)) {
            return text;
        }
        LocalDateTime dateTime = parseDateTime(text, this.getDateFormat());
        if (dateTime != null) {
            return toDate(dateTime);
        }
        dateTime = toLocalDateTime(super.value());
        if (dateTime != null) {
            return toDate(dateTime);
        }
        return text;
    }

    @Override
    public void formatValue() {
        this.setText(format(super.value(), this.getDateFormat()));
    }

    @Override
    public DateTimeTextFieldSkin skin() {
        return (DateTimeTextFieldSkin) super.skin();
    }

    @Override
    protected DateTimeTextFieldSkin createDefaultSkin() {
        return new DateTimeTextFieldSkin(this);
    }

    /**
     * 将值格式化为日期时间字符串
     *
     * @param value 值
     * @return 日期时间字符串
     */
    public static String format(Object value) {
        return format(value, null);
    }

    /**
     * 解析日期时间文本，兼容ISO、SQL、斜杠和紧凑格式。
     *
     * @param text 日期时间文本
     * @return 本地日期时间，无法解析时返回 null
     */
    public static LocalDateTime parseDateTime(CharSequence text) {
        return parseDateTime(text, null);
    }

    private static String format(Object value, SimpleDateFormat customFormat) {
        if (value == null) {
            return null;
        }
        LocalDateTime dateTime = toLocalDateTime(value);
        if (dateTime == null && value instanceof CharSequence sequence) {
            if (isCurrentTimestamp(sequence.toString())) {
                return sequence.toString();
            }
            dateTime = parseDateTime(sequence, customFormat);
            if (dateTime == null) {
                return sequence.toString();
            }
        }
        if (dateTime != null) {
            if (customFormat != null) {
                return copyOf(customFormat).format(toDate(dateTime));
            }
            return (dateTime.getNano() == 0 ? DEFAULT_FORMATTER : FRACTION_FORMATTER).format(dateTime);
        }
        return value.toString();
    }

    private static LocalDateTime parseDateTime(CharSequence text, SimpleDateFormat customFormat) {
        String value = text == null ? null : text.toString().strip();
        if (StringUtil.isBlank(value) || isCurrentTimestamp(value)) {
            return null;
        }
        if (customFormat != null) {
            LocalDateTime dateTime = parseCustom(value, customFormat);
            if (dateTime != null) {
                return dateTime;
            }
        }
        String normalized = normalizeIso(value);
        LocalDateTime dateTime = parseWithFormatter(normalized, DateTimeFormatter.ISO_DATE_TIME);
        if (dateTime != null) {
            return dateTime;
        }
        dateTime = parseWithFormatter(value, DateTimeFormatter.ISO_LOCAL_DATE);
        if (dateTime != null) {
            return dateTime;
        }
        for (DateTimeFormatter formatter : OTHER_FORMATTERS) {
            dateTime = parseWithFormatter(value, formatter);
            if (dateTime != null) {
                return dateTime;
            }
        }
        return null;
    }

    private static LocalDateTime parseWithFormatter(String text, DateTimeFormatter formatter) {
        try {
            return toLocalDateTime(formatter.parse(text));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static LocalDateTime parseCustom(String text, SimpleDateFormat format) {
        try {
            SimpleDateFormat parser = copyOf(format);
            parser.setLenient(false);
            ParsePosition position = new ParsePosition(0);
            Date value = parser.parse(text, position);
            if (value != null && position.getIndex() == text.length()) {
                return toLocalDateTime(value);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String normalizeIso(String text) {
        int separator = text.indexOf(' ');
        String value = text;
        if (separator > 0) {
            String date = text.substring(0, separator);
            if (date.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                value = date + "T" + text.substring(separator + 1).stripLeading();
            }
        }
        int zoneSeparator = value.lastIndexOf(' ');
        if (zoneSeparator > 0) {
            String zone = value.substring(zoneSeparator + 1);
            if (zone.equals("Z") || zone.startsWith("+") || zone.startsWith("-")) {
                value = value.substring(0, zoneSeparator) + zone;
            }
        }
        return value;
    }

    private static LocalDateTime toLocalDateTime(TemporalAccessor value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.from(value);
        } catch (Exception ignored) {
        }
        try {
            return OffsetDateTime.from(value).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return ZonedDateTime.from(value).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return Instant.from(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.from(value).atStartOfDay();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toLocalDateTime();
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.toLocalDateTime();
        }
        if (value instanceof Instant instant) {
            return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (value instanceof LocalDate date) {
            return date.atStartOfDay();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().atStartOfDay();
        }
        if (value instanceof Time time) {
            return LocalDate.now().atTime(time.toLocalTime());
        }
        if (value instanceof Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    private static Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static SimpleDateFormat copyOf(SimpleDateFormat format) {
        return (SimpleDateFormat) format.clone();
    }

    private static boolean isCurrentTimestamp(String text) {
        return StringUtil.equalsAnyIgnoreCase(text, "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP()");
    }
}
