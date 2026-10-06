package cn.oyzh.fx.db.sql;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDialect;

import java.util.List;

/**
 * sql解析器
 *
 * @author oyzh
 * @since 2024/1/26
 */
public abstract class DBSqlParser {

    protected final String sqlContent;

    protected final DBDialect dialect;

    public DBSqlParser(String sqlContent, DBDialect dialect) {
        this.sqlContent = sqlContent;
        this.dialect = dialect;
    }

    /**
     * 是否单语句
     *
     * @return 结果
     */
    public abstract boolean isSingle();

    /**
     * 解析sql
     *
     * @return 结果
     */
    public abstract List<String> parseSql();

    /**
     * 解析sql为单行
     *
     * @return 结果
     */
    public abstract String parseSingleSql();

    /**
     * 是否查询语句
     *
     * @param sql sql语句
     * @return 结果
     */
    public abstract boolean isSelect(String sql);

    /**
     * 美化sql语句
     *
     * @param sql sql语句
     * @return 结果
     */
    public abstract String prettySql(String sql);

    /**
     * 压缩sql语句
     *
     * @param sql sql语句
     * @return 结果
     */
    public abstract String compressSql(String sql);

    /**
     * 移除注释
     *
     * @param sql sql语句
     * @return 结果
     */
    public abstract String removeComment(String sql);

    /**
     * 是否查询全字段
     *
     * @param sql sql语句
     * @return 结果
     */
    public abstract boolean isFullColumn(String sql);

    public static String prettySql(String sql, DBDialect dialect) {
        return getParser(sql, dialect).prettySql(sql);
    }

    public static String compressSql(String sql, DBDialect dialect) {
        return getParser(sql, dialect).compressSql(sql);
    }

    public static List<String> parseSql(String sql, DBDialect dialect) {
        return getParser(sql, dialect).parseSql();
    }

    public static String parseSingleSql(String sql, DBDialect dialect) {
        return getParser(sql, dialect).parseSingleSql();
    }

    /**
     * sql解析器类型
     */
    public static String sqlParserType = "base";

    public static void setSqlParserType(String sqlParserType) {
        DBSqlParser.sqlParserType = sqlParserType;
    }

    public static DBSqlParser getParser(String sql, DBDialect dialect) {
        DBSqlParser sqlParser;
        if (StringUtil.equalsIgnoreCase("durid", sqlParserType)) {
            sqlParser = new DBDruidSqlParser(sql, dialect);
        } else {
            sqlParser = new DBBaseSqlParser(sql, dialect);
        }
        return sqlParser;
    }
}
