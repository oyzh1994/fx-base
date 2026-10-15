package cn.oyzh.fx.db.data.file.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

/**
 * Apache Commons CSV 格式构造工具。
 *
 * @author oyzh
 * @since 2026-10-14
 */
public final class DBDataCsvFormat {

    /**
     * CSV 默认字段分隔符
     */
    private static final String DEFAULT_DELIMITER = ",";

    /**
     * 私有构造函数
     */
    private DBDataCsvFormat() {
    }

    /**
     * 构造 CSV 格式。
     *
     * @param txtIdentifier   文本识别符，为空时不使用引号
     * @param recordSeparator 记录分隔符
     * @return CSV 格式
     */
    public static CSVFormat create(String txtIdentifier, String recordSeparator) {
        return create(DEFAULT_DELIMITER, txtIdentifier, recordSeparator);
    }

    /**
     * 构造定界文本格式。
     *
     * @param fieldSeparator  字段分隔符
     * @param txtIdentifier   文本识别符，为空时不使用引号
     * @param recordSeparator 记录分隔符
     * @return 定界文本格式
     */
    public static CSVFormat create(String fieldSeparator, String txtIdentifier, String recordSeparator) {
        Character quoteCharacter = quoteCharacter(txtIdentifier);
        CSVFormat.Builder builder = CSVFormat.DEFAULT.builder().setDelimiter(delimiter(fieldSeparator)).setIgnoreEmptyLines(false).setIgnoreSurroundingSpaces(false).setQuote(quoteCharacter).setRecordSeparator(recordSeparator).setTrim(false);
        if (quoteCharacter == null) {
            builder.setQuoteMode(QuoteMode.NONE);
        } else {
            // 所有字段均使用文本识别符包裹，保持原有 CSV 导出格式。
            builder.setQuoteMode(QuoteMode.ALL);
        }
        return builder.get();
    }

    /**
     * 校验字段分隔符。
     *
     * @param fieldSeparator 字段分隔符
     * @return 字段分隔符
     */
    private static String delimiter(String fieldSeparator) {
        if (fieldSeparator == null || fieldSeparator.isEmpty()) {
            throw new IllegalArgumentException("字段分隔符不能为空");
        }
        return fieldSeparator;
    }

    /**
     * 转换文本识别符配置。
     *
     * @param txtIdentifier 文本识别符
     * @return 单字符文本识别符，为空时返回 null
     */
    private static Character quoteCharacter(String txtIdentifier) {
        if (txtIdentifier == null || txtIdentifier.isEmpty()) {
            return null;
        }
        if (txtIdentifier.length() != 1) {
            throw new IllegalArgumentException("CSV 文本识别符必须为空或单个字符");
        }
        return txtIdentifier.charAt(0);
    }
}
