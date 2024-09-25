package cn.oyzh.fx.common.date;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author oyzh
 * @since 2024-09-25
 */
@UtilityClass
public class LocalDateUtil {

    public static LocalDate of(@NonNull Date date) {
        return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
