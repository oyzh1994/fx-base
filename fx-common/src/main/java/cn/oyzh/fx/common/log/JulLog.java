package cn.oyzh.fx.common.log;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author oyzh
 * @since 2024-09-27
 */
@UtilityClass
public class JulLog {

    private static final Logger LOGGER = Logger.getLogger("JULLog");

    static {
        setLevel(Level.ALL);
    }

    public static void setLevel(Level level) {
        LOGGER.setLevel(level);
    }

    public static void trace(String format, Object... args) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(format(Level.FINEST, format, args));
        }
    }

    public static void debug(String format, Object... args) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(format(Level.FINER, format, args));
        }
    }

    public static void info(String format, Object... args) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(format(Level.INFO, format, args));
        }
    }

    public static void warn(String format, Object... args) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.log(format(Level.WARNING, format, args));
        }
    }

    public static void error(String format, Object... args) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.log(format(Level.SEVERE, format, args));
        }
    }

    private static LogRecord format(Level level, String format, Object... args) {
        return format(level, format, null, args);
    }

    private static LogRecord format(Level level, String format, Throwable throwable) {
        return format(level, format, throwable, null);
    }

    private static LogRecord format(Level level, String format, Throwable throwable, Object... args) {
        if (StrUtil.isNotBlank(format) && ArrayUtil.isNotEmpty(args)) {
            int index = 0;
            while (format.contains("{}") && index < args.length) {
                format = format.replace("{}", "{" + index++ + "}");
            }
        }
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement element = trace[4];
        LogRecord logRecord = new LogRecord(level, format);
        logRecord.setParameters(args);
        logRecord.setThrown(throwable);
        logRecord.setSourceClassName(element.getClassName());
        logRecord.setLongThreadID(Thread.currentThread().threadId());
        logRecord.setSourceMethodName(element.getMethodName() + " " + element.getLineNumber());
        return logRecord;
    }
}
