package cn.oyzh.fx.db.sql;

import cn.oyzh.common.db.SqlUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDialect;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基于 Druid 的 SQL 解析器，借助 Druid 解析 SQL 并识别查询语句、全字段查询等
 *
 * @author oyzh
 * @since 2024-02-26
 */
public class DBDruidSqlParser extends DBSqlParser {

    /**
     * druid数据库类型
     */
    private final DbType dbType;

    /**
     * 构造数据库DruidSQL解析器对象。
     *
     * @param sqlContent SQL内容
     * @param dialect 方言
     */
    public DBDruidSqlParser(String sqlContent, DBDialect dialect) {
        super(sqlContent, dialect);
        this.dbType = switch (dialect) {
            case MYSQL -> DbType.mysql;
            case DAMENG -> DbType.dm;
            default -> null;
        };
    }

    @Override
    public String removeComment(String sql) {
        SQLStatement statement = SQLUtils.parseSingleStatement(sql, this.dbType, false);
        return SqlUtil.removeComments(statement.toString());
    }

    /**
     * 解析后的语句集合
     */
    private List<SQLStatement> sqlStatements;

    @Override
    public boolean isSingle() {
        // druid无法解析这些语句，直接返回
        if (this.dbType == DbType.mysql && StringUtil.startWithAnyIgnoreCase(sqlContent,
                "SHOW VARIABLES LIKE",
                "SHOW CREATE EVENT"
        )) {
            return true;
        }
        return this.sqlStatements != null && this.sqlStatements.size() == 1;
    }

    @Override
    public boolean isSelect(String sql) {
        // druid无法解析这些语句，直接返回
        if (this.dbType == DbType.mysql && StringUtil.startWithAnyIgnoreCase(sqlContent,
                "SHOW VARIABLES LIKE",
                "SHOW CREATE EVENT"
        )) {
            return true;
        }
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, this.dbType, SQLParserFeature.SkipComments);
        if (CollectionUtil.isNotEmpty(sqlStatements)) {
            SQLStatement statement = sqlStatements.getFirst();
            SchemaStatVisitor visitor = new SchemaStatVisitor(this.dbType);
            statement.accept(visitor);
            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            if (CollectionUtil.isNotEmpty(tables)) {
                TableStat stat = CollectionUtil.getFirst(tables.values());
                return stat != null && StringUtil.equalsIgnoreCase("Select", stat.toString());
            }
        }
        return false;
    }

    @Override
    public boolean isFullColumn(String sql) {
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, this.dbType, SQLParserFeature.SkipComments);
        if (CollectionUtil.isNotEmpty(sqlStatements)) {
            SQLStatement statement = sqlStatements.getFirst();
            SchemaStatVisitor visitor = new SchemaStatVisitor(this.dbType);
            statement.accept(visitor);
            Collection<TableStat.Column> columns = visitor.getColumns();
            if (CollectionUtil.isNotEmpty(columns)) {
                for (TableStat.Column column : columns) {
                    if (StringUtil.equals("*", column.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> parseSql() {
        String sqlContent = this.removeComment(this.sqlContent);
        List<String> sqlList = new ArrayList<>();
        try {
            this.sqlStatements = SQLUtils.parseStatements(sqlContent, this.dbType, SQLParserFeature.SkipComments);
            for (SQLStatement sqlStatement : this.sqlStatements) {
                String sql = sqlStatement.toString();
                sql = sql.replace("\n", " ");
                sqlList.add(sql);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            sqlList.add(sqlContent);
        }
        return sqlList;
    }

    @Override
    public String parseSingleSql() {
        String sqlContent = this.removeComment(this.sqlContent);
        String sql = null;
        try {

            SQLStatement statement = SQLUtils.parseSingleStatement(sqlContent, this.dbType, false);
            this.sqlStatements = new ArrayList<>();
            this.sqlStatements.add(statement);
            sql = statement.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            sql = sqlContent;
        }
        sql = sql.replace("\n", " ");
        return sql;
    }

    @Override
    public String prettySql(String sql) {
        try {
            SQLParserFeature[] features = new SQLParserFeature[]{
                    SQLParserFeature.KeepComments,
                    SQLParserFeature.KeepSelectListOriginalString
            };
            return SQLUtils.format(sql, this.dbType, null, null, features);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sql;
    }

    @Override
    public String compressSql(String sql) {
        try {
            // 压缩sql
            SQLUtils.FormatOption formatOption = new SQLUtils.FormatOption();
            formatOption.setUppCase(true);
            formatOption.setPrettyFormat(false);
            return SQLUtils.format(sql, this.dbType, formatOption);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sql;
    }
}
