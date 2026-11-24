package cn.oyzh.fx.db.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL查询词元，提供通用的SQL上下文、限定符和候选类型判断。
 *
 * @author oyzh
 * @since 2026-11-24
 */
public class DBSqlQueryToken extends DBQueryToken {

    /**
     * SQL上下文
     */
    private static final Pattern CONTEXT_PATTERN = Pattern.compile(
            "\\b(SELECT|FROM|JOIN|UPDATE|INTO|WHERE|ON|AND|OR|GROUP\\s+BY|ORDER\\s+BY|HAVING|SET|VALUES|CALL|EXEC|EXECUTE|CREATE|ALTER|DROP|INSERT|DELETE|MERGE|RETURNING|WHEN|THEN|ELSE|BETWEEN|LIKE|BY)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * 最近一次SQL上下文
     */
    private String context;

    /**
     * 点号前的限定符
     */
    private String qualifier;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    /**
     * 是否带限定符
     *
     * @return 结果
     */
    public boolean hasQualifier() {
        return this.qualifier != null && !this.qualifier.isBlank();
    }

    /**
     * 提取SQL上下文
     *
     * @param content 内容
     * @return 上下文
     */
    public static String parseContext(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        Matcher matcher = CONTEXT_PATTERN.matcher(content);
        String context = null;
        while (matcher.find()) {
            context = matcher.group(1).toUpperCase().replaceAll("\\s+", " ");
        }
        return context;
    }

    /**
     * 提取点号前的标识符
     *
     * @param content 内容
     * @return 标识符
     */
    public static String parseQualifier(String content) {
        if (content == null) {
            return null;
        }
        String text = content.stripTrailing();
        while (!text.isEmpty()) {
            char c = text.charAt(text.length() - 1);
            if (c == '"' || c == '`' || c == ']') {
                text = text.substring(0, text.length() - 1).stripTrailing();
            } else {
                break;
            }
        }
        int start = text.length();
        while (start > 0 && isIdentifierChar(text.charAt(start - 1))) {
            start--;
        }
        String qualifier = text.substring(start);
        return qualifier.isBlank() ? null : qualifier;
    }

    private static boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$' || c == '#';
    }

    /**
     * 是否可能是关键字
     *
     * @return 结果
     */
    public boolean isPossibilityKeyword() {
        char token = this.tokenChar();
        return token == '\0' || isWhitespace(token) || token == '(' || token == ','
                || token == ';' || token == '=' || token == '<' || token == '>'
                || token == '+' || token == '-' || token == '*' || token == '/';
    }

    /**
     * 是否可能是表
     *
     * @return 结果
     */
    public boolean isPossibilityTable() {
        return this.isObjectContext();
    }

    /**
     * 是否可能是视图
     *
     * @return 结果
     */
    public boolean isPossibilityView() {
        return this.isObjectContext();
    }

    /**
     * 是否可能是函数
     *
     * @return 结果
     */
    public boolean isPossibilityFunction() {
        return this.isObjectContext();
    }

    /**
     * 是否可能是存储过程
     *
     * @return 结果
     */
    public boolean isPossibilityProcedure() {
        return this.isObjectContext();
    }

    /**
     * 是否可能是序列
     *
     * @return 结果
     */
    public boolean isPossibilitySequence() {
        return this.isObjectContext();
    }

    /**
     * 是否可能是事件
     *
     * @return 结果
     */
    public boolean isPossibilityEvent() {
        return this.isObjectContext();
    }

    /**
     * 是否可能是数据库
     *
     * @return 结果
     */
    public boolean isPossibilityDatabase() {
        char token = this.tokenChar();
        return token == '\0' || isWhitespace(token) || token == '('
                || token == '"' || token == '`' || token == '[';
    }

    /**
     * 是否对象上下文
     *
     * @return 结果
     */
    protected boolean isObjectContext() {
        char token = this.tokenChar();
        return token == '\0' || isWhitespace(token) || token == '.' || token == ','
                || token == '(' || token == ')' || token == '"'
                || token == '`' || token == '[' || token == ']';
    }

    /**
     * 获取词元字符
     *
     * @return 字符
     */
    protected char tokenChar() {
        return this.getToken() == null ? '\0' : this.getToken();
    }

    /**
     * 是否空白字符
     *
     * @param c 字符
     * @return 结果
     */
    protected static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }
}
