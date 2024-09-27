package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.date.DateUtil;
import cn.oyzh.fx.common.jdbc.ColumnDefinition;
import cn.oyzh.fx.common.jdbc.JdbcConn;
import cn.oyzh.fx.common.jdbc.JdbcHelper;
import cn.oyzh.fx.common.jdbc.JdbcManager;
import cn.oyzh.fx.common.jdbc.JdbcOperator;
import cn.oyzh.fx.common.jdbc.JdbcUtil;
import cn.oyzh.fx.common.jdbc.TableDefinition;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-23
 */
public class SqliteOperator extends JdbcOperator {

    public SqliteOperator(TableDefinition tableDefinition) {
        super(tableDefinition);
    }

    @Override
    public boolean initTable() throws Exception {
        JdbcConn connection = JdbcManager.takeoff();
        try {
            String tableName = this.tableName();
            ResultSet resultSet = connection.getTables(tableName);
            boolean exists = resultSet.next();
            resultSet.close();
            if (exists) {
                boolean changed = false;
                for (ColumnDefinition column : this.tableDefinition.getColumns()) {
                    ResultSet resultSet1 = connection.getMetaData().getColumns(null, null, tableName, column.getColumnName());
                    // 字段不存在
                    if (!resultSet1.next()) {
                        changed = true;
                    } else {
                        // 字段类型不相同
                        String typeName = resultSet1.getString("TYPE_NAME");
                        if (!StrUtil.equalsIgnoreCase(typeName, column.getColumnType())) {
                            changed = true;
                        }
                    }
                    resultSet1.close();
                    if (changed) {
                        break;
                    }
                }
                if (changed) {
                    this.alterTable();
                }
            } else {
                this.createTable();
            }
            return true;
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    @Override
    protected void alterTable() throws SQLException {
        JdbcConn connection = JdbcManager.takeoff();
        try {
            // 旧表更名
            String tableName = this.tableName();
            String oldTableName = tableName + "_" + DateUtil.format("yyyyMMdd") + "_old_table";
            StringBuilder sql = new StringBuilder("ALTER TABLE ");
            sql.append(SqlLiteUtil.wrap(tableName))
                    .append(" RENAME TO ")
                    .append(SqlLiteUtil.wrap(oldTableName));
            JdbcHelper.executeUpdate(connection, sql.toString());

            // 创建新表
            this.createTable();

            // 查询新表字段
            ResultSet resultSet1 = connection.getMetaData().getColumns(null, null, tableName, null);
            List<String> cols1 = new ArrayList<>();
            while (resultSet1.next()) {
                cols1.add(resultSet1.getString("COLUMN_NAME"));
            }
            resultSet1.close();

            // 查询旧表字段
            ResultSet resultSet2 = connection.getMetaData().getColumns(null, null, oldTableName, null);
            List<String> cols2 = new ArrayList<>();
            while (resultSet2.next()) {
                cols2.add(resultSet2.getString("COLUMN_NAME"));
            }
            resultSet2.close();

            // 获取交集
            List<String> cols3 = cols1.stream().filter(cols2::contains).toList();

            // 交集为空则直接返回
            if (cols3.isEmpty()) {
                return;
            }

            // 转移数据
            StringBuilder sql1 = new StringBuilder("INSERT INTO ");
            sql1.append(SqlLiteUtil.wrap(tableName))
                    .append("(");
            for (String col : cols3) {
                sql1.append(SqlLiteUtil.wrap(col)).append(",");
            }
            sql1.deleteCharAt(sql1.length() - 1);
            sql1.append(") SELECT");
            for (String col : cols3) {
                sql1.append(SqlLiteUtil.wrap(col)).append(",");
            }
            sql1.deleteCharAt(sql1.length() - 1);
            sql1.append(" FROM ")
                    .append(SqlLiteUtil.wrap(oldTableName));
            JdbcHelper.execute(connection, sql1.toString());
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    @Override
    protected void createTable() throws SQLException {
        JdbcConn connection = JdbcManager.takeoff();
        try {
            String tableName = this.tableName();
            StringBuilder sql1 = new StringBuilder();
            sql1.append("CREATE TABLE ")
                    .append(SqlLiteUtil.wrap(tableName))
                    .append(" (");
            for (ColumnDefinition columnDefinition : this.tableDefinition.getColumns()) {
                sql1.append(SqlLiteUtil.wrap(columnDefinition.getColumnName()))
                        .append(" ")
                        .append(columnDefinition.getColumnType());
                if (columnDefinition.isPrimaryKey()) {
                    sql1.append(" primary key");
                }
                sql1.append(",");
            }
            sql1.deleteCharAt(sql1.length() - 1);
            sql1.append(")");
            JdbcHelper.executeUpdate(connection, sql1.toString());
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    @Override
    public int delete(Map<String, Object> params, Long limit) throws SQLException {
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(JdbcUtil.wrap(tableName));
        if (CollUtil.isNotEmpty(params)) {
            boolean first = true;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" AND ");
                }
                sql.append(entry.getKey());
                sql.append("=");
                sql.append(JdbcUtil.wrapData(entry.getValue()));
            }
        }
        if (limit != null && limit > 0) {
            if (CollUtil.isEmpty(params)) {
                sql.append(" WHERE ");
            } else {
                sql.append(" AND ");
            }
            sql.append(" rowid IN ( SELECT rowid FROM ")
                    .append(tableName)
                    .append(" LIMIT ")
                    .append(limit)
                    .append(")");
        }
        JdbcConn connection = JdbcManager.takeoff();
        try {
            return JdbcHelper.executeUpdate(connection, sql.toString());
        } finally {
            JdbcManager.giveback(connection);
        }
    }
}
