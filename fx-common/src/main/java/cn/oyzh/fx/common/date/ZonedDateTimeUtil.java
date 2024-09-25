package cn.oyzh.fx.common.date;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author oyzh
 * @since 2024-09-25
 */
@UtilityClass
public class ZonedDateTimeUtil {

    public static ZonedDateTime of(@NonNull Date date) {
        return of(date.toInstant());
    }

    public static ZonedDateTime of(@NonNull Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
