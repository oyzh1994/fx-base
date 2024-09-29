package cn.oyzh.fx.common.h2;

import cn.oyzh.fx.common.jdbc.ColumnDefinition;
import cn.oyzh.fx.common.jdbc.DeleteParam;
import cn.oyzh.fx.common.jdbc.JdbcConn;
import cn.oyzh.fx.common.jdbc.JdbcHelper;
import cn.oyzh.fx.common.jdbc.JdbcManager;
import cn.oyzh.fx.common.jdbc.JdbcOperator;
import cn.oyzh.fx.common.jdbc.JdbcUtil;
import cn.oyzh.fx.common.jdbc.QueryParam;
import cn.oyzh.fx.common.jdbc.TableDefinition;
import cn.oyzh.fx.common.util.CollectionUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            List<String> deletedColumns = new ArrayList<>();
            List<ColumnDefinition> addedColumns = new ArrayList<>();
            List<ColumnDefinition> changedColumns = new ArrayList<>();
            for (ColumnDefinition column : this.columns()) {
                ResultSet resultSet = connection.getColumns(tableName, column.getColumnName());
                try (resultSet) {
                    // 字段不存在
                    if (!resultSet.next()) {
                        addedColumns.add(column);
                        continue;
                    }
                    // 字段类型不相同
                    String typeName = resultSet.getString("TYPE_NAME");
                    if (!H2Util.checkSqlType(column.getColumnType(), typeName)) {
                        changedColumns.add(column);
                    }
                }
            }
            ResultSet resultSet = connection.getColumns(tableName, null);
            try (resultSet) {
                while (resultSet.next()) {
                    // 字段被删除
                    String columnName = resultSet.getString("COLUMN_NAME");
                    if (!this.tableDefinition.hasColumn(columnName)) {
                        deletedColumns.add(columnName);
                    }
                }
            }
            if (!deletedColumns.isEmpty() || !addedColumns.isEmpty() || !changedColumns.isEmpty()) {
                // 删除
                for (String column : deletedColumns) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("ALTER TABLE ");
                    sql.append(JdbcUtil.wrap(tableName));
                    sql.append(" DROP COLUMN ");
                    sql.append(H2Util.wrap(column));
                    JdbcHelper.executeUpdate(connection, sql.toString());
                }
                // 修改
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
                // 新增
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
    public int delete(DeleteParam deleteParam) throws SQLException {
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(JdbcUtil.wrap(tableName));
        if (CollectionUtil.isNotEmpty(deleteParam.getQueryParams())) {
            boolean first = true;
            for (QueryParam queryParam : deleteParam.getQueryParams()) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" AND ");
                }
                sql.append(queryParam.getName());
                sql.append(queryParam.getOperator());
                sql.append(JdbcUtil.wrapData(queryParam.getData()));
            }
        }
        if (deleteParam.getLimit() != null && deleteParam.getLimit() > 0) {
            sql.append(" LIMIT ").append(deleteParam.getLimit());
        }
        JdbcConn connection = JdbcManager.takeoff();
        try {
            return JdbcHelper.executeUpdate(connection, sql.toString());
        } finally {
            JdbcManager.giveback(connection);
        }
    }
}
