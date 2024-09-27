package cn.oyzh.fx.common.h2;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
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
public class H2Operator extends JdbcOperator {

    public H2Operator(TableDefinition tableDefinition) {
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
                this.alterTable();
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
            String tableName = this.tableName();
            List<ColumnDefinition> addedColumns = new ArrayList<>();
            List<ColumnDefinition> changedColumns = new ArrayList<>();
            for (ColumnDefinition column : this.columns()) {
                ResultSet resultSet1 = connection.getColumns(tableName, column.getColumnName());
                // 字段不存在
                if (!resultSet1.next()) {
                    addedColumns.add(column);
                } else {
                    // 字段类型不相同
                    String typeName = resultSet1.getString("TYPE_NAME");
                    if (!StrUtil.equalsIgnoreCase(typeName, column.getColumnType())) {
                        changedColumns.add(column);
                    }
                }
                resultSet1.close();
            }
            if (!addedColumns.isEmpty() || !changedColumns.isEmpty()) {
                for (ColumnDefinition column : addedColumns) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("ALTER TABLE ");
                    sql.append(JdbcUtil.wrap(tableName));
                    sql.append(" ADD COLUMN ");
                    sql.append(H2Util.wrap(column.getColumnName()));
                    sql.append(" ");
                    sql.append(column.getColumnType().toUpperCase());
                    if (column.isPrimaryKey()) {
                        sql.append(" PRIMARY KEY");
                    }
                    JdbcHelper.executeUpdate(connection, sql.toString());
                }
                for (ColumnDefinition column : changedColumns) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("ALTER TABLE ");
                    sql.append(JdbcUtil.wrap(tableName));
                    sql.append(" ALTER COLUMN ");
                    sql.append(H2Util.wrap(column.getColumnName()));
                    sql.append(" ");
                    sql.append(column.getColumnType().toUpperCase());
                    if (column.isPrimaryKey()) {
                        sql.append(" NOT NULL");
                    }
                    JdbcHelper.executeUpdate(connection, sql.toString());
                }
            }
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    @Override
    protected void createTable() throws SQLException {
        JdbcConn connection = JdbcManager.takeoff();
        try {
            String tableName = this.tableName();
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ")
                    .append(H2Util.wrap(tableName))
                    .append(" (");
            for (ColumnDefinition column : this.columns()) {
                sql.append(H2Util.wrap(column.getColumnName()))
                        .append(" ")
                        .append(column.getColumnType().toUpperCase());
                if (column.isPrimaryKey()) {
                    sql.append(" PRIMARY KEY");
                }
                sql.append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
            JdbcHelper.executeUpdate(connection, sql.toString());
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
            sql.append(" LIMIT ").append(limit);
        }
        JdbcConn connection = JdbcManager.takeoff();
        try {
            return JdbcHelper.executeUpdate(connection, sql.toString());
        } finally {
            JdbcManager.giveback(connection);
        }
    }
}
