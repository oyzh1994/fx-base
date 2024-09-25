package cn.oyzh.fx.common.date;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author oyzh
 * @since 2024-09-25
 */
@UtilityClass
public class LocalTimeUtil {

    public static LocalTime of(@NonNull Date date) {
        return of(date.toInstant());
    }

    public static LocalTime of(@NonNull Instant instant) {
        return LocalTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
