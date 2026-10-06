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

    public abstract String removeComment();

    // public abstract DBSqlNodes parseNode() throws Exception;

    public abstract boolean isSingle();

    public abstract boolean isSelect();

    public abstract boolean isFullColumn();

    public abstract List<String> parseSql() throws Exception;

    public abstract String parseSingleSql() throws Exception;

    public abstract String prettySql() throws Exception;

    public abstract String compressSql() throws Exception;

    public static String prettySql(String sql, DBDialect dialect) throws Exception {
        return getParser(sql, dialect).prettySql();
    }

    public static List<String> parseSql(String sql, DBDialect dialect) throws Exception {
        return getParser(sql, dialect).parseSql();
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
