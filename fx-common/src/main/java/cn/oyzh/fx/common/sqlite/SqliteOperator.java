package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.jdbc.JdbcResultSet;
import cn.oyzh.fx.common.jdbc.JdbcUtil;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-23
 */
public class SqliteOperator {

    @Getter
    private final TableDefinition tableDefinition;

    public SqliteOperator(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    private PrimaryKeyColumn getPrimaryKeyColumn(Object primaryKey) {
        ColumnDefinition columnDefinition = this.tableDefinition.primaryKeyColumn();
        if (columnDefinition == null) {
            return null;
        }
        return new PrimaryKeyColumn(columnDefinition.getColumnName(), primaryKey);
    }

    /**
     * 执行初始化
     *
     * @return 结果
     */
    public boolean initTable() throws Exception {
        Connection connection = SqliteConnManager.takeoff();
        try {
            String tableName = this.tableDefinition.getTableName();
            String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            JdbcResultSet resultSet = JdbcUtil.executeQuery(connection, sql, tableName);
            boolean exists = resultSet.next();
            resultSet.close();
            if (exists) {
                for (ColumnDefinition columnDefinition : this.tableDefinition.getColumns()) {
                    ResultSet resultSet1 = connection.getMetaData().getColumns(null, null, tableName, columnDefinition.getColumnName());
                    if (!resultSet1.next()) {
                        StringBuilder sql1 = new StringBuilder();
                        sql1.append("ALTER TABLE ")
                                .append(SqlLiteUtil.wrap(tableName))
                                .append(" ADD COLUMN ")
                                .append(SqlLiteUtil.wrap(columnDefinition.getColumnName()))
                                .append(" ")
                                .append(columnDefinition.getColumnType());
                        if (columnDefinition.isPrimaryKey()) {
                            sql1.append(" primary key not null");
                        }
                        sql1.append(";");
                        JdbcUtil.executeUpdate(connection, sql1.toString());
                    }
                    resultSet1.close();
                }
            } else {
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
                JdbcUtil.executeUpdate(connection, sql1.toString());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqliteConnManager.giveback(connection);
        }
        return false;
    }

    public int insert(Map<String, Object> record) throws Exception {
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName);
        sql.append("(");
        for (String column : record.keySet()) {
            sql.append(column).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES(");
        sql.append("?,".repeat(record.size()));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        Connection connection = SqliteConnManager.takeoff();
        try {
            List<Object> values = new ArrayList<>();
            for (String key : record.keySet()) {
                values.add(record.get(key));
            }
            return JdbcUtil.executeUpdate(connection, sql.toString(), values);
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public int update(Map<String, Object> record, Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? 0 : this.update(record, primaryKeyColumn);
    }

    public int update(Map<String, Object> record, PrimaryKeyColumn primaryKey) throws Exception {
        record.remove(primaryKey.getColumnName());
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName);
        sql.append(" SET ");
        for (String column : record.keySet()) {
            sql.append(column).append("=?,");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append("=?");
        Connection connection = SqliteConnManager.takeoff();
        try {
            List<Object> values = new ArrayList<>();
            for (String key : record.keySet()) {
                values.add(record.get(key));
            }
            values.add(primaryKey.getColumnData());
            return JdbcUtil.executeUpdate(connection, sql.toString(), values);
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public boolean exist(Object primaryKey) throws SQLException {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn != null && this.exist(primaryKeyColumn);
    }

    public boolean exist(PrimaryKeyColumn primaryKey) throws SQLException {
        String tableName = this.tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append("=?");
        Connection connection = SqliteConnManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcUtil.executeQuery(connection, sql.toString(), primaryKey.getColumnData());
            boolean exists = false;
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
            }
            resultSet.close();
            return exists;
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public boolean exist(Map<String, Object> params) throws SQLException {
        String tableName = this.tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(tableName);
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
                sql.append(SqlLiteUtil.wrapData(entry.getValue()));
            }
        }
        Connection connection = SqliteConnManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcUtil.executeQuery(connection, sql.toString());
            boolean exists = false;
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
            }
            resultSet.close();
            return exists;
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public Map<String, Object> selectOne(Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? null : this.selectOne(primaryKeyColumn);
    }

    public Map<String, Object> selectOne(PrimaryKeyColumn primaryKey) throws SQLException {
        String tableName = this.tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append("=?");
        Connection connection = SqliteConnManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcUtil.executeQuery(connection, sql.toString(), primaryKey.getColumnData());
            Map<String, Object> record = new HashMap<>();
            while (resultSet.next()) {
                for (ColumnDefinition columnDefinition : tableDefinition.getColumns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.findColumn(columnName) >= 0) {
                        record.put(columnName, resultSet.getObject(columnName));
                    }
                }
            }
            resultSet.close();
            return record;
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public List<Map<String, Object>> selectList(List<QueryParam> params) throws SQLException {
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        if (CollUtil.isNotEmpty(params)) {
            boolean first = true;
            for (QueryParam param : params) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" AND ");
                }
                sql.append(param.getName());
                sql.append(param.getOperator());
                sql.append(SqlLiteUtil.wrapData(param.getData()));
            }
        }
        Connection connection = SqliteConnManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcUtil.executeQuery(connection, sql.toString());
            List<Map<String, Object>> records = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> record = new HashMap<>();
                for (ColumnDefinition columnDefinition : tableDefinition.getColumns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.findColumn(columnName) >= 0) {
                        record.put(columnName, resultSet.getObject(columnName));
                    }
                }
                records.add(record);
            }
            resultSet.close();
            return records;
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public long selectCount(String kw, List<String> columns) throws SQLException {
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(tableName);
        if (StrUtil.isNotBlank(kw) && CollUtil.isNotEmpty(columns)) {
            boolean first = true;
            for (String column : columns) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" OR ");
                }
                sql.append(column);
                sql.append(" LIKE ");
                sql.append(SqlLiteUtil.wrapData("%" + kw + "%"));
            }
        }
        Connection connection = SqliteConnManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcUtil.executeQuery(connection, sql.toString());
            long count = 0;
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
            resultSet.close();
            return count;
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public List<Map<String, Object>> selectPage(String kw, List<String> columns, PageParam pageParam) throws SQLException {
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        if (StrUtil.isNotBlank(kw) && CollUtil.isNotEmpty(columns)) {
            boolean first = true;
            for (String column : columns) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" OR ");
                }
                sql.append(column);
                sql.append(" LIKE ");
                sql.append(SqlLiteUtil.wrapData("%" + kw + "%"));
            }
        }
        sql.append(" LIMIT ")
                .append(pageParam.getLimit())
                .append(" OFFSET ")
                .append(pageParam.getStart());
        Connection connection = SqliteConnManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcUtil.executeQuery(connection, sql.toString());
            List<Map<String, Object>> records = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> record = new HashMap<>();
                for (ColumnDefinition columnDefinition : tableDefinition.getColumns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.findColumn(columnName) >= 0) {
                        record.put(columnName, resultSet.getObject(columnName));
                    }
                }
                records.add(record);
            }
            resultSet.close();
            return records;
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public int delete(Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? 0 : this.delete(primaryKeyColumn);
    }

    public int delete(PrimaryKeyColumn primaryKey) throws SQLException {
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append("=?");
        Connection connection = SqliteConnManager.takeoff();
        try {
            return JdbcUtil.executeUpdate(connection, sql.toString(), primaryKey.getColumnData());
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }

    public int delete(Map<String, Object> params) throws SQLException {
        if (CollUtil.isEmpty(params)) {
            return 0;
        }
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(tableName);
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
            sql.append(SqlLiteUtil.wrapData(entry.getValue()));
        }
        Connection connection = SqliteConnManager.takeoff();
        try {
            return JdbcUtil.executeUpdate(connection, sql.toString());
        } finally {
            SqliteConnManager.giveback(connection);
        }
    }
}
