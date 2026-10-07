package cn.oyzh.fx.db.data.dto;


import java.nio.charset.StandardCharsets;

/**
 * 数据库数据导出配置，用于控制导出时的日期格式、字段与记录分割符、文本识别符及字符集等参数。
 *
 * @author oyzh
 * @since 2024-09-02
 */
public class DBDataExportConfig {

    /**
     * 日期格式
     */
    private String dateFormat;

    /**
     * 字段作为属性
     */
    private boolean fieldToAttr;

    /**
     * 是否包含列标题
     */
    private boolean includeFields = true;

    /**
     * 记录分割符号
     */
    private String recordSeparator = System.lineSeparator();

    /**
     * 字段分割符号
     */
    private String fieldSeparator = ";";

    /**
     * 文本识别符号
     */
    private String txtIdentifier = "\"";

    /**
     * 字符集
     */
    private String charset = StandardCharsets.UTF_8.displayName();

    /**
     * 是否使用早期版本格式
     */
    private boolean earlyVersion;

    /**
     * 出错时是否继续
     */
    private boolean continueWithError;

    /**
     * 获取日期格式。
     *
     * @return 日期格式
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * 设置日期格式。
     *
     * @param dateFormat 日期格式
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * 是否字段到属性。
     *
     * @return 字段到属性
     */
    public boolean isFieldToAttr() {
        return fieldToAttr;
    }

    /**
     * 设置字段到属性。
     *
     * @param fieldToAttr 字段到属性
     */
    public void setFieldToAttr(boolean fieldToAttr) {
        this.fieldToAttr = fieldToAttr;
    }

    /**
     * 是否包含字段集合。
     *
     * @return 包含字段集合
     */
    public boolean isIncludeFields() {
        return includeFields;
    }

    /**
     * 设置包含字段集合。
     *
     * @param includeFields 包含字段集合
     */
    public void setIncludeFields(boolean includeFields) {
        this.includeFields = includeFields;
    }

    /**
     * 获取记录分隔符。
     *
     * @return 记录分隔符
     */
    public String getRecordSeparator() {
        return recordSeparator;
    }

    /**
     * 设置记录分隔符。
     *
     * @param recordSeparator 记录分隔符
     */
    public void setRecordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
    }

    /**
     * 获取字段分隔符。
     *
     * @return 字段分隔符
     */
    public String getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * 设置字段分隔符。
     *
     * @param fieldSeparator 字段分隔符
     */
    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * 获取文本标识符。
     *
     * @return 文本标识符
     */
    public String getTxtIdentifier() {
        return txtIdentifier;
    }

    /**
     * 设置文本标识符。
     *
     * @param txtIdentifier 文本标识符
     */
    public void setTxtIdentifier(String txtIdentifier) {
        this.txtIdentifier = txtIdentifier;
    }

    /**
     * 获取字符集。
     *
     * @return 字符集
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置字符集。
     *
     * @param charset 字符集
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * 是否早期版本。
     *
     * @return 早期版本
     */
    public boolean isEarlyVersion() {
        return earlyVersion;
    }

    /**
     * 设置早期版本。
     *
     * @param earlyVersion 早期版本
     */
    public void setEarlyVersion(boolean earlyVersion) {
        this.earlyVersion = earlyVersion;
    }

    /**
     * 是否继续错误。
     *
     * @return 继续错误
     */
    public boolean isContinueWithError() {
        return continueWithError;
    }

    /**
     * 设置继续错误。
     *
     * @param continueWithError 继续错误
     */
    public void setContinueWithError(boolean continueWithError) {
        this.continueWithError = continueWithError;
    }
}
