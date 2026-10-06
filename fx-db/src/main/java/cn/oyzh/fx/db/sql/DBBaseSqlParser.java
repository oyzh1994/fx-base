package cn.oyzh.fx.db.sql;

import cn.oyzh.common.db.SqlDatabase;
import cn.oyzh.common.db.SqlUtil;
import cn.oyzh.fx.db.DBDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2026/10026
 */
public class DBBaseSqlParser extends DBSqlParser {

    private final SqlDatabase database;

    public DBBaseSqlParser(String sqlContent, DBDialect dialect) {
        super(sqlContent, dialect);
        this.database = switch (dialect) {
            case MYSQL -> SqlDatabase.MYSQL;
            case DAMENG -> SqlDatabase.DM;
            default -> SqlDatabase.ANSI;
        };
    }

    @Override
    public String removeComment(String sql) {
        return SqlUtil.removeComments(sql, this.database);
    }

    private List<String> sqlList;

    @Override
    public boolean isSingle() {
        return this.sqlList != null && this.sqlList.size() == 1;
    }

    @Override
    public boolean isSelect(String sql) {
        return SqlUtil.isQuery(sql, this.database);
    }

    @Override
    public boolean isFullColumn(String sql) {
        return SqlUtil.isAllFieldQuery(sql, this.database);
    }

    @Override
    public List<String> parseSql() {
        String sqlContent = this.removeComment(this.sqlContent);
        List<String> sqlList = new ArrayList<>();
        boolean success = false;
        try {
            this.sqlList = SqlUtil.split(sqlContent, this.database);
            for (String sql : this.sqlList) {
                sql = sql.replace("\n", " ");
                sqlList.add(sql);
            }
            success = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!success) {
            sqlList.add(sqlContent);
        }
        return sqlList;
    }

    @Override
    public String parseSingleSql() {
        String sqlContent = this.removeComment(this.sqlContent);
        String sql = SqlUtil.singleStatement(sqlContent, this.database);
        if (sql != null) {
            sql = sql.replace("\n", " ");
            this.sqlList = new ArrayList<>();
            this.sqlList.add(sql);
        }
        return sql;
    }

    @Override
    public String prettySql(String sql) {
        try {
            return SqlUtil.format(sql, this.database);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sql;
    }

    @Override
    public String compressSql(String sql) {
        try {
            return SqlUtil.compressSql(sql, this.database);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sql;
    }
}
