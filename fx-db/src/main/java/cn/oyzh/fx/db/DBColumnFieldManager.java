package cn.oyzh.fx.db;

import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据库字段定义管理器，按方言维护字段定义并提供类型能力查询
 *
 * @author oyzh
 * @since 2026-08-26
 */
public class DBColumnFieldManager {

    /**
     * 方言字段定义的初始化器
     */
    private static final Map<DBDialect, Runnable> INITIALIZERS = new ConcurrentHashMap<>();

    /**
     * 方言对应的字段定义列表
     */
    private static final Map<DBDialect, List<DBColumnField>> COLUMN_FIELD = new ConcurrentHashMap<>();

    /**
     * 注册方言字段定义的初始化器
     *
     * @param dialect 方言
     * @param func    初始化器
     */
    public static void registerInitializer(DBDialect dialect, Runnable func) {
        INITIALIZERS.put(dialect, func);
    }

    /**
     * 添加字段定义
     *
     * @param dialect     方言
     * @param columnField 字段定义
     */
    public static void putFiled(DBDialect dialect, DBColumnField columnField) {
        if (dialect == null) {
            throw new NullPointerException("dialect");
        }
        if (columnField == null) {
            throw new NullPointerException("columnField");
        }
        List<DBColumnField> list = COLUMN_FIELD.get(dialect);
        if (list == null) {
            list = new ArrayList<>();
            list.add(columnField);
            COLUMN_FIELD.put(dialect, list);
        } else {
            list.add(columnField);
        }
    }

    /**
     * 获取方言的字段定义列表，必要时触发初始化
     *
     * @param dialect 方言
     * @return 字段定义列表
     */
    public static List<DBColumnField> fields(DBDialect dialect) {
        synchronized (COLUMN_FIELD) {
            if (!COLUMN_FIELD.containsKey(dialect)) {
                Runnable func = INITIALIZERS.remove(dialect);
                if (func != null) {
                    func.run();
                }
            }
        }
        List<DBColumnField> list = COLUMN_FIELD.get(dialect);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * 获取方言的字段名称列表
     *
     * @param dialect 方言
     * @return 字段名称列表
     */
    public static List<String> fieldNames(DBDialect dialect) {
        return fields(dialect).parallelStream().map(DBColumnField::getName).collect(Collectors.toList());
    }

    /**
     * 判断指定类型是否支持长度
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportSize(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportSize;
            }
        }
        return false;
    }

    /**
     * 获取指定类型的推荐长度
     *
     * @param dialect 方言
     * @param type    类型
     * @return 推荐长度
     */
    public static Integer suggestSize(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.suggestSize;
            }
        }
        return null;
    }

    /**
     * 判断指定类型是否支持无符号
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportUnsigned(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportUnsigned;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持json
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportJson(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportJson;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持键长度
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportKeySize(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportKeySize;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持字符串
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportString(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportString;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持文本
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportText(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportText;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持值
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportValue;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持填充零
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportZeroFill(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportZeroFill;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持bit类型
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportBit(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBit;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持二进制
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportBinary(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBinary;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持小数
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportDigits(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportDigits;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持默认值
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportDefaultValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportDefaultValue;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持几何
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportGeometry(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportGeometry;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持枚举
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportEnum(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportEnum;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持字符集
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportCharset(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportCharset;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持时间戳
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportTimestamp(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportTimestamp;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持整数
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportInteger(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportInteger;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持自动递增
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportAutoIncrement(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportAutoIncrement;
            }
        }
        return false;
    }

    /**
     * 获取指定类型的示例值
     *
     * @param dialect 方言
     * @param type    类型
     * @return 示例值
     */
    public static Object exampleValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.exampleValue;
            }
        }
        return false;
    }

    /**
     * 获取指定类型的默认值
     *
     * @param dialect 方言
     * @param type    类型
     * @return 默认值
     */
    public static Object defaultValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.defaultValue;
            }
        }
        return false;
    }

    /**
     * 获取指定类型的最小值
     *
     * @param dialect 方言
     * @param type    类型
     * @return 最小值
     */
    public static Long minValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.minValue;
            }
        }
        return null;
    }

    /**
     * 获取指定类型的最大值
     *
     * @param dialect 方言
     * @param type    类型
     * @return 最大值
     */
    public static Long maxValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.maxValue;
            }
        }
        return null;
    }

    /**
     * 判断指定类型是否支持json数组
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportJsonArray(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportJsonArray;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持长整数
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportBigInteger(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBigInteger;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否支持布尔
     *
     * @param dialect 方言
     * @param type    类型
     * @return 结果
     */
    public static boolean supportBoolean(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBoolean;
            }
        }
        return false;
    }
}
