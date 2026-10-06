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
    public String removeComment() {
        return SqlUtil.removeComments(this.sqlContent, this.database);
    }

    private Boolean single;

    private Boolean select;

    private List<String> sqlList;

    @Override
    public boolean isSingle() {
        if (this.single != null) {
            return this.single;
        }
        return this.sqlList != null && this.sqlList.size() == 1;
    }

    @Override
    public boolean isSelect() {
        if (this.select != null) {
            return this.select;
        }
        return SqlUtil.isQuery(this.sqlContent, this.database);
    }

    @Override
    public boolean isFullColumn() {
        return SqlUtil.isAllFieldQuery(this.sqlContent, this.database);
    }

    @Override
    public List<String> parseSql() {
        String sqlContent = this.removeComment();
        List<String> sqlList = new ArrayList<>();
        boolean success = false;
        try {
            this.sqlList = SqlUtil.split(sqlContent, this.database);
            for (String sql : this.sqlList) {
                sql = sql.replace("\n", " ");
                sqlList.add(sql);
            }
            this.single = null;
            this.select = null;
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
        String sqlContent = this.removeComment();
        String sql = SqlUtil.singleStatement(sqlContent, this.database);
        if (sql != null) {
            sql = sql.replace("\n", " ");
            this.sqlList = new ArrayList<>();
            this.sqlList.add(sql);
        }
        return sql;
    }

    @Override
    public String prettySql() {
        try {
            return SqlUtil.format(this.sqlContent, this.database);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.sqlContent;
    }

    @Override
    public String compressSql() throws Exception {
        return SqlUtil.compressSql(this.sqlContent, this.database);
    }
}
