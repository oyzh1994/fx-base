package cn.oyzh.fx.db.query;

/**
 * SQL查询提示分类，用于按SQL上下文调整候选优先级。
 *
 * @author oyzh
 * @since 2026-11-24
 */
public enum DBSqlQueryPromptCategory {

    KEYWORD,
    DATABASE,
    TABLE,
    VIEW,
    FUNCTION,
    PROCEDURE,
    SEQUENCE,
    EVENT
}
