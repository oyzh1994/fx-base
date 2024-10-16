package cn.oyzh.fx.common.date;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author oyzh
 * @since 2024/7/2
 */
@UtilityClass
public class DateUtil {

    public static Date of(@NonNull Number n) {
        return of(n.longValue());
    }

    public static Date of(long l) {
        return new Date(l);
    }

    public static String format(String format) {
        return format(format, System.currentTimeMillis());
    }

    public static String format(String format, long time) {
        return new SimpleDateFormat(format).format(time);
    }
}
