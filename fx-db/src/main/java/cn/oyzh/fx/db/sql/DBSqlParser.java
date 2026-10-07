package cn.oyzh.fx.db.sql;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDialect;

import java.util.List;

/**
 * SQL 解析器抽象基类，定义 SQL 的解析、美化、压缩及注释移除等通用能力
 *
 * @author oyzh
 * @since 2024-01-26
 */
public abstract class DBSqlParser {

    /**
     * sql内容
     */
    protected final String sqlContent;

    /**
     * 数据库方言
     */
    protected final DBDialect dialect;

    /**
     * 构造数据库SQL解析器对象。
     *
     * @param sqlContent SQL内容
     * @param dialect 方言
     */
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

    /**
     * 美化sql语句
     *
     * @param sql     sql语句
     * @param dialect 数据库方言
     * @return 美化后的sql语句
     */
    public static String prettySql(String sql, DBDialect dialect) {
        return getParser(sql, dialect).prettySql(sql);
    }

    /**
     * 压缩sql语句
     *
     * @param sql     sql语句
     * @param dialect 数据库方言
     * @return 压缩后的sql语句
     */
    public static String compressSql(String sql, DBDialect dialect) {
        return getParser(sql, dialect).compressSql(sql);
    }

    /**
     * 解析sql
     *
     * @param sql     sql语句
     * @param dialect 数据库方言
     * @return 解析后的sql列表
     */
    public static List<String> parseSql(String sql, DBDialect dialect) {
        return getParser(sql, dialect).parseSql();
    }

    /**
     * 解析sql为单行
     *
     * @param sql     sql语句
     * @param dialect 数据库方言
     * @return 解析后的单行sql
     */
    public static String parseSingleSql(String sql, DBDialect dialect) {
        return getParser(sql, dialect).parseSingleSql();
    }

    /**
     * sql解析器类型
     */
    public static String sqlParserType = "base";

    /**
     * 设置sql解析器类型
     *
     * @param sqlParserType sql解析器类型
     */
    public static void setSqlParserType(String sqlParserType) {
        DBSqlParser.sqlParserType = sqlParserType;
    }

    /**
     * 获取sql解析器
     *
     * @param sql     sql语句
     * @param dialect 数据库方言
     * @return sql解析器
     */
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
