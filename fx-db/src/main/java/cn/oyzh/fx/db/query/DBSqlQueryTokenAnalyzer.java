package cn.oyzh.fx.db.query;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SQL查询词元解析器基类，统一处理词元边界、SQL上下文、候选评分和排序。
 *
 * @author oyzh
 * @since 2026-11-24
 */
public abstract class DBSqlQueryTokenAnalyzer<E extends DBQueryPromptItem, T extends DBSqlQueryToken>
        extends DBQueryTokenAnalyzer<E, T> {

    protected static final byte TYPE_DATABASE = 1;

    protected static final byte TYPE_TABLE = 2;

    protected static final byte TYPE_KEYWORD = 4;

    protected static final byte TYPE_VIEW = 5;

    protected static final byte TYPE_FUNCTION = 6;

    protected static final byte TYPE_PROCEDURE = 7;

    protected static final byte TYPE_EVENT = 8;

    protected static final byte TYPE_SEQUENCE = 8;

    private static final double EMPTY_CORRELATION = 0.55;

    private static final int MAX_PROMPTS = 100;

    private static final Set<String> TABLE_CONTEXTS = Set.of(
            "FROM", "JOIN", "UPDATE", "INTO", "REFERENCES", "TABLE", "VIEW", "DELETE", "INSERT", "MERGE");

    private static final Set<String> EXPRESSION_CONTEXTS = Set.of(
            "SELECT", "WHERE", "ON", "AND", "OR", "SET", "HAVING", "WHEN", "THEN", "ELSE",
            "BETWEEN", "LIKE", "BY", "GROUP BY", "ORDER BY", "RETURNING", "VALUES");

    private static final Set<String> ROUTINE_CONTEXTS = Set.of(
            "CALL", "EXEC", "EXECUTE", "CREATE", "ALTER", "DROP");

    /**
     * 创建词元
     *
     * @return 词元
     */
    protected abstract T createToken();

    /**
     * 创建提示项
     *
     * @return 提示项
     */
    protected abstract E createPromptItem();

    @Override
    public T currentToken(String content, int currentIndex) {
        String source = content == null ? "" : content;
        int endIndex = Math.max(0, Math.min(currentIndex, source.length()));
        String beforeCaret = source.substring(0, endIndex);
        int tokenIndex = -1;
        char tokenType = '\0';
        for (int i = beforeCaret.length() - 1; i >= 0; i--) {
            char c = beforeCaret.charAt(i);
            if (isSeparator(c)) {
                tokenIndex = i;
                tokenType = c;
                break;
            }
        }
        int startIndex = tokenType == '\0' ? 0 : tokenIndex + 1;
        String prefix = tokenType == '\0'
                ? beforeCaret.substring(0, startIndex)
                : beforeCaret.substring(0, tokenIndex);
        T token = this.createToken();
        token.setToken(tokenType);
        token.setStartIndex(startIndex);
        token.setEndIndex(endIndex);
        token.setContent(beforeCaret.substring(startIndex).strip());
        token.setContext(DBSqlQueryToken.parseContext(prefix));
        if (tokenType == '.') {
            token.setQualifier(DBSqlQueryToken.parseQualifier(prefix));
        }
        return token;
    }

    /**
     * 是否词元分隔符
     *
     * @param c 字符
     * @return 结果
     */
    protected boolean isSeparator(char c) {
        return Character.isWhitespace(c)
                || c == '.' || c == ',' || c == '`' || c == '"'
                || c == '(' || c == ')' || c == '[' || c == ']'
                || c == ';' || c == '=' || c == '+' || c == '-' || c == '*'
                || c == '/' || c == '%' || c == '<' || c == '>' || c == '|';
    }

    /**
     * 添加提示项
     *
     * @param items      提示项列表
     * @param token      词元
     * @param text       当前输入
     * @param minCorr    最低相关度
     * @param type       提示类型
     * @param content    提示内容
     * @param extContent 扩展内容
     * @param qualifier  限定符匹配值
     * @param category   提示分类
     */
    protected void addPrompt(List<E> items, T token, String text, float minCorr,
                             byte type, String content, String extContent, String qualifier,
                             DBSqlQueryPromptCategory category) {
        if (content == null || content.isBlank() || !this.matchesQualifier(token, qualifier)) {
            return;
        }
        double correlation = this.correlation(content, text);
        if (correlation <= minCorr) {
            return;
        }
        correlation += this.contextBoost(token.getContext(), category);
        E item = this.createPromptItem();
        item.setType(type);
        item.setContent(content);
        item.setExtContent(extContent);
        item.setCorrelation(correlation);
        items.add(item);
    }

    /**
     * 计算候选关联度
     *
     * @param candidate 候选内容
     * @param input     输入内容
     * @return 关联度
     */
    protected double correlation(String candidate, String input) {
        if (input == null || input.isEmpty()) {
            return EMPTY_CORRELATION;
        }
        String left = candidate.toUpperCase();
        String right = input.toUpperCase();
        if (left.equals(right)) {
            return 2.0;
        }
        double lengthRatio = (double) Math.min(left.length(), right.length())
                / Math.max(left.length(), right.length());
        int index = left.indexOf(right);
        if (index == 0) {
            return 1.2 + 0.35 * lengthRatio;
        }
        if (index > 0) {
            double score = this.isWordBoundary(left, index) ? 0.95 : 0.80;
            return score + 0.20 * lengthRatio;
        }
        int commonLength = this.commonSubsequenceLength(left, right);
        double coverage = (double) commonLength / right.length();
        if (coverage < 0.55) {
            return 0;
        }
        double candidateCoverage = (double) commonLength / left.length();
        double score = 0.35 + 0.45 * coverage + 0.20 * candidateCoverage;
        if (left.length() < right.length()) {
            score *= 0.60;
        }
        return score;
    }

    /**
     * 计算上下文加权
     *
     * @param context  SQL上下文
     * @param category 提示分类
     * @return 加权值
     */
    protected double contextBoost(String context, DBSqlQueryPromptCategory category) {
        if (context == null) {
            return category == DBSqlQueryPromptCategory.KEYWORD ? 0.15 : 0;
        }
        if (TABLE_CONTEXTS.contains(context)) {
            return switch (category) {
                case TABLE, VIEW -> 0.45;
                case DATABASE -> 0.25;
                case FUNCTION, PROCEDURE, SEQUENCE, EVENT -> 0.10;
                default -> 0;
            };
        }
        if (EXPRESSION_CONTEXTS.contains(context)) {
            return switch (category) {
                case FUNCTION -> 0.12;
                case TABLE, VIEW -> 0.05;
                default -> 0;
            };
        }
        if (ROUTINE_CONTEXTS.contains(context)) {
            return switch (category) {
                case PROCEDURE -> 0.40;
                case FUNCTION -> 0.25;
                case SEQUENCE, EVENT -> 0.15;
                case TABLE, VIEW -> 0.15;
                default -> 0;
            };
        }
        return 0;
    }

    /**
     * 排序并限制提示数量
     *
     * @param items 提示列表
     * @return 结果
     */
    protected List<E> sortPrompts(List<E> items) {
        return items.parallelStream()
                .sorted(Comparator.comparingDouble(DBQueryPromptItem::getCorrelation).reversed())
                .limit(MAX_PROMPTS)
                .collect(Collectors.toList());
    }

    private boolean matchesQualifier(T token, String qualifier) {
        if (!token.hasQualifier()) {
            return true;
        }
        return qualifier != null && qualifier.equalsIgnoreCase(token.getQualifier());
    }

    private boolean isWordBoundary(String content, int index) {
        if (index <= 0) {
            return true;
        }
        char previous = content.charAt(index - 1);
        return previous == '_' || previous == '$' || previous == '#' || previous == '.';
    }

    private int commonSubsequenceLength(String left, String right) {
        int[] lengths = new int[right.length() + 1];
        for (int i = 0; i < left.length(); i++) {
            int previous = 0;
            for (int j = 1; j <= right.length(); j++) {
                int current = lengths[j];
                if (left.charAt(i) == right.charAt(j - 1)) {
                    lengths[j] = previous + 1;
                } else {
                    lengths[j] = Math.max(lengths[j], lengths[j - 1]);
                }
                previous = current;
            }
        }
        return lengths[right.length()];
    }
}
