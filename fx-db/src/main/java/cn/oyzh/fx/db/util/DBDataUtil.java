package cn.oyzh.fx.db.util;

import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBDialect;

import java.util.Objects;

/**
 *
 * @author oyzh
 * @since 2026-09-06
 */
public class DBDataUtil {

    /**
     * 转义符号
     *
     * @param str     内容
     * @param dialect 方言
     * @return 转义后的内容
     */
    public static String escapeQuotes(String str, DBDialect dialect) {
        return TextUtil.escape(str, c -> {
            if (dialect == DBDialect.MYSQL || dialect == DBDialect.DAMENG) {
                // 如果是'字符，则返回''字符
                if (Objects.equals(c, '\'')) {
                    return "''";
                }
            }
            return null;
        });
    }


    //    /**
    //     * 参数化，json
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForJson(DBColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return DateHelper.formatDate(date);
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //            if (value instanceof Date date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //        }
    //        if (column.supportJson()) {
    //            return value.toString();
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return value.toString();
    //        }
    //        if (column.supportString()) {
    //            return TextUtil.escape((String) value);
    //        }
    //        if (column.supportInteger() || column.supportDigits()) {
    //            return value;
    //        }
    //        return value.toString();
    //    }

    //    /**
    //     * 参数化，xml
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForXml(DBColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return DateHelper.formatDate(date);
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //            if (value instanceof Date date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //        }
    //        if (column.supportJson()) {
    //            return value.toString();
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return value.toString();
    //        }
    //        if (column.supportString()) {
    //            return TextUtil.escape((String) value);
    //        }
    //        if (column.supportInteger() || column.supportDigits()) {
    //            return value;
    //        }
    //        return value.toString();
    //    }
    //
    //    /**
    //     * 参数化，csv
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForCsv(DBColumn column, Object value) {
    //        if (value == null) {
    //            return "";
    //        }
    //        if (column.supportGeometry()) {
    //            return "\"ST_GeomFromText('" + value + "')\"";
    //        }
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return "\"" + DateHelper.formatDate(date) + "\"";
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return "\"" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "\"";
    //            }
    //            if (value instanceof Date date) {
    //                return "\"" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "\"";
    //            }
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "\"b'" + TextUtil.byteToBitStr(bytes) + "'\"";
    //        }
    //        if (column.supportString()) {
    //            return "\"" + TextUtil.escape((String) value) + "\"";
    //        }
    //        return "\"" + value + "\"";
    //    }

    //    /**
    //     * 参数化，sql
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForSql(DBColumn column, Object value, DBDialect dialect) {
    //        //        if (value == null) {
    //        //            return "NULL";
    //        //        }
    //        //        if (column.supportGeometry()) {
    //        //            return "ST_GeomFromText('" + value + "')";
    //        //        }
    //        //        if (column.isDateType()) {
    //        //            Date date = (Date) value;
    //        //            return "'" + DateHelper.formatDate(date) + "'";
    //        //        }
    //        //        if (column.supportTimestamp()) {
    //        //            return "'" + value + "'";
    //        //        }
    //        //        if (column.supportJson()) {
    //        //            return "'" + value + "'";
    //        //        }
    //        //        if (column.supportBinary()) {
    //        //            byte[] bytes = (byte[]) value;
    //        //            if (bytes.length == 0) {
    //        //                return "NULL";
    //        //            }
    //        //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        //        }
    //        //        if (column.supportBoolean()) {
    //        //            if (value instanceof Boolean b) {
    //        //                return b ? "1" : "0";
    //        //            }
    //        //        }
    //        //        if (column.supportBit()) {
    //        //            if (value instanceof Boolean b) {
    //        //                return b ? "1" : "0";
    //        //            }
    //        //            byte[] bytes = (byte[]) value;
    //        //            if (bytes.length == 0) {
    //        //                return "NULL";
    //        //            }
    //        //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        //        }
    //        //        if (column.supportEnum()) {
    //        //            return "'" + value + "'";
    //        //        }
    //        //        if (column.supportString()) {
    //        //            String str = TextUtil.escape((String) value);
    //        //            return "\"" + str + "\"";
    //        //        }
    //        //        return value;
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
    //        return DBUtil.wrapData(value, dialect);
    //    }

    /**
     * 参数化，sql
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForSql(DBColumn column, Object value, DBDialect dialect) {
        if (value == null) {
            return "NULL";
        }
        if (column.supportGeometry()) {
            return "ST_GeomFromText('" + value + "')";
        }
        //        if (column.supportTimestamp() || column.isDateTimeType() || column.isDateType() || column.isTimeType()) {
        //            Date date = (Date) value;
        //            value = DateHelper.formatDate(date);
        //        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "NULL";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "NULL";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        //        if (column.supportString()) {
        //            value = DBDataUtil.escapeQuotes((String) value, dialect);
        //        }
        return DBUtil.wrapData(value, dialect);
    }

    //    /**
    //     * 参数化，html
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForHtml(DBColumn column, Object value) {
    //        if (value == null) {
    //            return "";
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return DateHelper.formatDate(date);
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //            if (value instanceof Date date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //        }
    //        if (column.supportJson()) {
    //            return value.toString();
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return value.toString();
    //        }
    //        if (column.supportString()) {
    //            return TextUtil.escape((String) value);
    //        }
    //        if (column.supportInteger() || column.supportDigits()) {
    //            return value;
    //        }
    //        return value.toString();
    //    }
    //
    //    /**
    //     * 参数化，xls
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForXls(DBColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
    //        if (column.isDateType()) {
    //            if (value instanceof Date date) {
    //                return DateHelper.formatDate(date);
    //            }
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return DateUtil.format(date, "yyyy/M/dd HH:mm:ss");
    //            }
    //            if (value instanceof Date date) {
    //                return DateUtil.format(date, "yyyy/M/dd HH:mm:ss");
    //            }
    //        }
    //        if (column.supportJson()) {
    //            return value.toString();
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return value.toString();
    //        }
    //        if (column.supportString()) {
    //            return TextUtil.escape((String) value);
    //        }
    //        return value;
    //    }
}
