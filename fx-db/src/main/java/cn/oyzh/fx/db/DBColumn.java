package cn.oyzh.fx.db;

import cn.oyzh.common.util.StringUtil;

/**
 * 数据库列接口，提供列的名称、类型、长度等读写及类型能力判断
 *
 * @author oyzh
 * @since 2026-09-01
 */
public interface DBColumn extends DBName {

    /**
     * 获取类型
     *
     * @return 结果
     */
    String getType();

    /**
     * 设置长度
     *
     * @param size 长度
     */
    void setSize(Integer size);

    /**
     * 获取长度
     *
     * @return 结果
     */
    Integer getSize();

    /**
     * 设置类型
     *
     * @param type 类型
     */
    void setType(String type);

    /**
     * 是否自动递增
     *
     * @return 结果
     */
    default boolean isAutoIncrement() {
        return false;
    }

    /**
     * 是否支持长度
     *
     * @return 结果
     */
    default boolean supportSize() {
        return DBColumnFieldManager.supportSize(this.getDialect(), this.getType());
    }

    /**
     * 是否支持几何
     *
     * @return 结果
     */
    default boolean supportGeometry() {
        return DBColumnFieldManager.supportGeometry(this.getDialect(), this.getType());
    }

    /**
     * 是否支持字符集
     *
     * @return 结果
     */
    default boolean supportCharset() {
        return DBColumnFieldManager.supportCharset(this.getDialect(), this.getType());
    }

    /**
     * 是否支持无符号
     *
     * @return 结果
     */
    default boolean supportUnsigned() {
        return DBColumnFieldManager.supportUnsigned(this.getDialect(), this.getType());
    }

    /**
     * 是否支持小数
     *
     * @return 结果
     */
    default boolean supportDigits() {
        return DBColumnFieldManager.supportDigits(this.getDialect(), this.getType());
    }

    /**
     * 是否支持整数
     *
     * @return 结果
     */
    default boolean supportInteger() {
        return DBColumnFieldManager.supportInteger(this.getDialect(), this.getType());
    }

    /**
     * 是否支持长整数
     *
     * @return 结果
     */
    default boolean supportBigInteger() {
        return DBColumnFieldManager.supportBigInteger(this.getDialect(), this.getType());
    }

    /**
     * 是否支持自动递增
     *
     * @return 结果
     */
    default boolean supportAutoIncrement() {
        return !this.isGenerated() && DBColumnFieldManager.supportAutoIncrement(this.getDialect(), this.getType());
    }

    /**
     * 是否生成的字段
     *
     * @return 结果
     */
    default boolean isGenerated() {
        return false;
    }

    /**
     * 是否支持默认值
     *
     * @return 结果
     */
    default boolean supportDefaultValue() {
        return !this.isGenerated() && DBColumnFieldManager.supportDefaultValue(this.getDialect(), this.getType());
    }

    /**
     * 是否支持时间戳
     *
     * @return 结果
     */
    default boolean supportTimestamp() {
        return DBColumnFieldManager.supportTimestamp(this.getDialect(), this.getType());
    }

    /**
     * 是否支持值
     *
     * @return 结果
     */
    default boolean supportValue() {
        return DBColumnFieldManager.supportValue(this.getDialect(), this.getType());
    }

    /**
     * 是否支持文本
     *
     * @return 结果
     */
    default boolean supportText() {
        return DBColumnFieldManager.supportText(this.getDialect(), this.getType());
    }

    /**
     * 是否支持填充零
     *
     * @return 结果
     */
    default boolean supportZeroFill() {
        return DBColumnFieldManager.supportZeroFill(this.getDialect(), this.getType());
    }

    /**
     * 是否支持bit类型
     *
     * @return 结果
     */
    default boolean supportBit() {
        return DBColumnFieldManager.supportBit(this.getDialect(), this.getType());
    }

    /**
     * 是否支持json
     *
     * @return 结果
     */
    default boolean supportJson() {
        return DBColumnFieldManager.supportJson(this.getDialect(), this.getType());
    }

    /**
     * 是否支持json数组
     *
     * @return 结果
     */
    default boolean supportJsonArray() {
        return DBColumnFieldManager.supportJsonArray(this.getDialect(), this.getType());
    }

    /**
     * 是否支持键长度
     *
     * @return 结果
     */
    default boolean supportKeySize() {
        return DBColumnFieldManager.supportKeySize(this.getDialect(), this.getType());
    }

    /**
     * 是否支持字符串
     *
     * @return 结果
     */
    default boolean supportString() {
        return DBColumnFieldManager.supportString(this.getDialect(), this.getType());
    }

    /**
     * 是否支持布尔
     *
     * @return 结果
     */
    default boolean supportBoolean() {
        return DBColumnFieldManager.supportBoolean(this.getDialect(), this.getType());
    }

    /**
     * 是否支持二进制
     *
     * @return 结果
     */
    default boolean supportBinary() {
        return DBColumnFieldManager.supportBinary(this.getDialect(), this.getType());
    }

    /**
     * 是否支持枚举
     *
     * @return 结果
     */
    default boolean supportEnum() {
        return DBColumnFieldManager.supportEnum(this.getDialect(), this.getType());
    }

    /**
     * 获取方言
     *
     * @return 结果
     */
    DBDialect getDialect();

    /**
     * 是否主键
     *
     * @return 是否主键
     */
    default boolean isPrimaryKey() {
        return false;
    }

    /**
     * 设置是否主键
     *
     * @param primaryKey 是否主键
     */
    default void setPrimaryKey(boolean primaryKey) {

    }

    /**
     * 获取最小值
     *
     * @return 结果
     */
    default Long minValue() {
        return null;
    }

    /**
     * 获取最大值
     *
     * @return 结果
     */
    default Long maxValue() {
        return null;
    }

    /**
     * 获取示例值
     *
     * @return 结果
     */
    default Object exampleValue() {
        return null;
    }

    /**
     * 是否年类型
     *
     * @return 结果
     */
    default boolean isYearType() {
        return false;
    }

    /**
     * 是否时间类型
     *
     * @return 结果
     */
    default boolean isTimeType() {
        return false;
    }

    /**
     * 是否日期类型
     *
     * @return 结果
     */
    default boolean isDateType() {
        return false;
    }

    /**
     * 是否日期时间类型
     *
     * @return 结果
     */
    default boolean isDateTimeType() {
        return false;
    }

    /**
     * 获取浮点位
     *
     * @return 结果
     */
    default Integer getDigits() {
        return null;
    }

    /**
     * 获取默认值
     *
     * @return 结果
     */
    default Object getDefaultValue() {
        return null;
    }

    /**
     * 默认值是否序列
     *
     * @return 结果
     */
    default boolean isDefaultValueSequence() {
        return this.getDefaultValue() instanceof String s && StringUtil.endsWithIgnoreCase(s, ".NEXTVAL");
    }

    /**
     * 获取序列名称
     *
     * @return 结果
     */
    default String getSequenceName() {
        if (!isDefaultValueSequence()) {
            return null;
        }
        String def = this.getDefaultValue().toString();
        return def.substring(0, def.lastIndexOf("."));
    }

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName()) || StringUtil.isBlank(this.getType());
    }

    /**
     * 获取架构
     *
     * @return 结果
     */
    default String getSchema() {
        return null;
    }

    /**
     * 获取数据库名称
     *
     * @return 结果
     */
    default String getDbName() {
        return null;
    }

    /**
     * 获取表名称
     *
     * @return 结果
     */
    default String getTableName() {
        return null;
    }
}
