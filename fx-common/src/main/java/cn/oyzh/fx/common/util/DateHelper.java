package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author oyzh
 * @since 2024/7/2
 */
@UtilityClass
public class DateHelper {

    /**
     * 全局通用日期格式化对象
     */
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 全局通用日期格式化对象
     */
    public static final SimpleDateFormat DATE_TIME_SIMPLE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 全局通用日期-时间格式化对象
     */
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * 全局通用日期-时间格式化对象
     */
    public static final SimpleDateFormat TIME_SIMPLE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * 全局通用日期-时间格式化对象
     */
    public static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

    /**
     * 全局通用日期-时间格式化对象
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatDateTime() {
        return DATE_TIME_FORMAT.format(System.currentTimeMillis());
    }

    public static String formatDateTime(Date date) {
        return DATE_TIME_FORMAT.format(date);
    }

    public static String formatDateTimeSimple() {
        return DATE_TIME_SIMPLE_FORMAT.format(System.currentTimeMillis());
    }

    public static String formatDateTimeSimple(Date date) {
        return DATE_TIME_SIMPLE_FORMAT.format(date);
    }

    public static String formatYear() {
        return YEAR_FORMAT.format(System.currentTimeMillis());
    }

    public static String formatYear(Date date) {
        return YEAR_FORMAT.format(date);
    }

    public static String formatTime() {
        return TIME_FORMAT.format(System.currentTimeMillis());
    }

    public static String formatTime(Date date) {
        return TIME_FORMAT.format(date);
    }

    public static String formatTimeSimple() {
        return TIME_SIMPLE_FORMAT.format(System.currentTimeMillis());
    }

    public static String formatTimeSimple(Date date) {
        return TIME_SIMPLE_FORMAT.format(date);
    }

    public static String formatDate() {
        return DATE_FORMAT.format(System.currentTimeMillis());
    }

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }
}
