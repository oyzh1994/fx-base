package cn.oyzh.fx.common;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;

/**
 * 常量对象
 *
 * @author oyzh
 * @since 2020/9/14
 */
@UtilityClass
public class Const {

    /**
     * 全局通用日期格式化对象
     */
    public final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 全局通用日期-时间格式化对象
     */
    public final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

}
