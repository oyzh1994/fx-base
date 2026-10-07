package cn.oyzh.fx.db.data.dto;


import cn.oyzh.common.util.StringUtil;

import java.nio.charset.StandardCharsets;

/**
 * 数据库数据导入配置，用于控制导入模式、字段与记录分割符、文本识别符及字符集等参数。
 *
 * @author oyzh
 * @since 2024/09/02
 */
public class DBDataImportConfig {

    /**
     * 日期格式
     */
    private String dateFormat;

    /**
     * 导入模式
     * 1. 追加
     * 2. 复制
     */
    private String importMode = "2";

    /**
     * 字段标题行索引
     */
    private int columnIndex = 0;

    /**
     * 数据起始行索引
     */
    private int dataStartIndex = 1;

    /**
     * 记录标签（包装数据的对象键名）
     */
    private String recordLabel;

    /**
     * 是否将属性作为字段
     */
    private boolean attrToColumn;

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
     * 是否为追加模式
     *
     * @return 结果
     */
    public boolean isAppendMode() {
        return StringUtil.equals(this.importMode, "1");
    }

    /**
     * 是否为复制模式
     *
     * @return 结果
     */
    public boolean isCopyMode() {
        return StringUtil.equals(this.importMode, "2");
    }

    /**
     * 获取字段分割符字符
     *
     * @return 结果
     */
    public char fieldSeparatorChar() {
        return this.fieldSeparator.charAt(0);
    }

    /**
     * 获取文本识别符字符
     *
     * @return 结果
     */
    public char txtIdentifierChar() {
        return this.txtIdentifier.charAt(0);
    }

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
     * 获取导入模式。
     *
     * @return 导入模式
     */
    public String getImportMode() {
        return importMode;
    }

    /**
     * 设置导入模式。
     *
     * @param importMode 导入模式
     */
    public void setImportMode(String importMode) {
        this.importMode = importMode;
    }

    /**
     * 获取列索引。
     *
     * @return 列索引
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * 设置列索引。
     *
     * @param columnIndex 列索引
     */
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * 获取数据开始索引。
     *
     * @return 数据开始索引
     */
    public int getDataStartIndex() {
        return dataStartIndex;
    }

    /**
     * 设置数据开始索引。
     *
     * @param dataStartIndex 数据开始索引
     */
    public void setDataStartIndex(int dataStartIndex) {
        this.dataStartIndex = dataStartIndex;
    }

    /**
     * 获取记录标签。
     *
     * @return 记录标签
     */
    public String getRecordLabel() {
        return recordLabel;
    }

    /**
     * 设置记录标签。
     *
     * @param recordLabel 记录标签
     */
    public void setRecordLabel(String recordLabel) {
        this.recordLabel = recordLabel;
    }

    /**
     * 是否属性到列。
     *
     * @return 属性到列
     */
    public boolean isAttrToColumn() {
        return attrToColumn;
    }

    /**
     * 设置属性到列。
     *
     * @param attrToColumn 属性到列
     */
    public void setAttrToColumn(boolean attrToColumn) {
        this.attrToColumn = attrToColumn;
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
}
