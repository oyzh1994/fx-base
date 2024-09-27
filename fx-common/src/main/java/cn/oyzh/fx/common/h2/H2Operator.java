package cn.oyzh.fx.common.h2;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.jdbc.ColumnDefinition;
import cn.oyzh.fx.common.jdbc.JdbcManager;
import cn.oyzh.fx.common.jdbc.JdbcHelper;
import cn.oyzh.fx.common.jdbc.JdbcResultSet;
import cn.oyzh.fx.common.jdbc.PageParam;
import cn.oyzh.fx.common.jdbc.PrimaryKeyColumn;
import cn.oyzh.fx.common.jdbc.QueryParam;
import cn.oyzh.fx.common.jdbc.SelectListParam;
import cn.oyzh.fx.common.jdbc.TableDefinition;
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
public class H2Operator {

    @Getter
    private final TableDefinition tableDefinition;

    public H2Operator(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    private String tableName() {
        return this.tableDefinition.getTableName();
    }

    private PrimaryKeyColumn getPrimaryKeyColumn(Object primaryKey) {
        ColumnDefinition columnDefinition = this.tableDefinition.primaryKey();
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
        Connection connection = JdbcManager.takeoff();
        try {
            String tableName = this.tableDefinition.getTableName();
            ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName.toUpperCase(), null);
            boolean exists = resultSet.next();
            resultSet.close();
            if (exists) {
                this.alterTable();
            } else {
                this.createTable();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcManager.giveback(connection);
        }
        return false;
    }

    protected void alterTable() throws SQLException {
        Connection connection = JdbcManager.takeoff();
        try {
            String tableName = this.tableName();
            List<ColumnDefinition> addedColumns = new ArrayList<>();
            List<ColumnDefinition> changedColumns = new ArrayList<>();
            for (ColumnDefinition column : this.tableDefinition.getColumns()) {
                ResultSet resultSet1 = connection.getMetaData().getColumns(null, null, tableName.toUpperCase(), column.getColumnName().toUpperCase());
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
                    sql.append(tableName.toUpperCase());
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
                    sql.append(tableName.toUpperCase());
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

    protected void createTable() throws SQLException {
        Connection connection = JdbcManager.takeoff();
        try {
            String tableName = this.tableName();
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ")
                    .append(H2Util.wrap(tableName))
                    .append(" (");
            for (ColumnDefinition column : this.tableDefinition.getColumns()) {
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

    public int insert(Map<String, Object> record) throws Exception {
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName);
        sql.append("(");
        for (String column : record.keySet()) {
            sql.append(H2Util.wrap(column)).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES(");
        sql.append("?,".repeat(record.size()));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        Connection connection = JdbcManager.takeoff();
        try {
            List<Object> values = new ArrayList<>();
            for (String key : record.keySet()) {
                values.add(record.get(key));
            }
            return JdbcHelper.executeUpdate(connection, sql.toString(), values);
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    public int update(Map<String, Object> record, Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? 0 : this.update(record, primaryKeyColumn);
    }

    public int update(Map<String, Object> record, PrimaryKeyColumn primaryKey) throws Exception {
        record.remove(primaryKey.getColumnName());
        String tableName = this.tableName();
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
        Connection connection = JdbcManager.takeoff();
        try {
            List<Object> values = new ArrayList<>();
            for (String key : record.keySet()) {
                values.add(record.get(key));
            }
            values.add(primaryKey.getColumnData());
            return JdbcHelper.executeUpdate(connection, sql.toString(), values);
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    public boolean exist(Object primaryKey) throws SQLException {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn != null && this.exist(primaryKeyColumn);
    }

    public boolean exist(PrimaryKeyColumn primaryKey) throws SQLException {
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append("=?");
        Connection connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString(), primaryKey.getColumnData());
            boolean exists = false;
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
            }
            resultSet.close();
            return exists;
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    public boolean exist(Map<String, Object> params) throws SQLException {
        String tableName = this.tableName();
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
                sql.append(H2Util.wrapData(entry.getValue()));
            }
        }
        Connection connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
            boolean exists = false;
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
            }
            resultSet.close();
            return exists;
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    public Map<String, Object> selectOne(Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? null : this.selectOne(primaryKeyColumn);
    }

    public Map<String, Object> selectOne(PrimaryKeyColumn primaryKey) throws SQLException {
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append("=?");
        Connection connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString(), primaryKey.getColumnData());
            Map<String, Object> record = new HashMap<>();
            while (resultSet.next()) {
                for (ColumnDefinition columnDefinition : this.tableDefinition.getColumns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.findColumn(columnName) >= 0) {
                        record.put(columnName, resultSet.getObject(columnName));
                    }
                }
            }
            resultSet.close();
            return record;
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    public Map<String, Object> selectOne(QueryParam param) throws SQLException {
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(param.getName());
        sql.append("=");
        sql.append(H2Util.wrapData(param.getData()));
        Connection connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
            Map<String, Object> record = new HashMap<>();
            while (resultSet.next()) {
                for (ColumnDefinition columnDefinition : this.tableDefinition.getColumns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.findColumn(columnName) >= 0) {
                        record.put(columnName, resultSet.getObject(columnName));
                    }
                }
            }
            resultSet.close();
            return record;
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    public List<Map<String, Object>> selectList(SelectListParam param) throws SQLException {
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT ");
        if (CollUtil.isEmpty(param.getQueryColumns())) {
            sql.append("*");
        } else {
            for (String queryColumn : param.getQueryColumns()) {
                sql.append(queryColumn).append(",");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
        }
        sql.append(" FROM ");
        sql.append(tableName);
        if (CollUtil.isNotEmpty(param.getQueryParams())) {
            boolean first = true;
            for (QueryParam queryParam : param.getQueryParams()) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" AND ");
                }
                sql.append(queryParam.getName());
                sql.append(queryParam.getOperator());
                sql.append(H2Util.wrapData(queryParam.getData()));
            }
        }
        Connection connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
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
            JdbcManager.giveback(connection);
        }
    }

    public long selectCount(List<QueryParam> params) throws SQLException {
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
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
                sql.append(H2Util.wrapData(param.getData()));
            }
        }
        Connection connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
            long count = 0;
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
            resultSet.close();
            return count;
        } finally {
            JdbcManager.giveback(connection);
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
                sql.append(H2Util.wrapData("%" + kw + "%"));
            }
        }
        Connection connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
            long count = 0;
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
            resultSet.close();
            return count;
        } finally {
            JdbcManager.giveback(connection);
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
                sql.append(H2Util.wrapData("%" + kw + "%"));
            }
        }
        sql.append(" LIMIT ")
                .append(pageParam.getLimit())
                .append(" OFFSET ")
                .append(pageParam.getStart());
        Connection connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
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
            JdbcManager.giveback(connection);
        }
    }

    public int delete(Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? 0 : this.delete(primaryKeyColumn);
    }

    public int delete(PrimaryKeyColumn primaryKey) throws SQLException {
        String tableName = this.tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append("=?");
        Connection connection = JdbcManager.takeoff();
        try {
            return JdbcHelper.executeUpdate(connection, sql.toString(), primaryKey.getColumnData());
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    public int delete(Map<String, Object> params, Long limit) throws SQLException {
        String tableName = this.tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("DELETE FROM ");
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
                sql.append(H2Util.wrapData(entry.getValue()));
            }
        }
        if (limit != null && limit > 1) {
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
        Connection connection = JdbcManager.takeoff();
        try {
            return JdbcHelper.executeUpdate(connection, sql.toString());
        } finally {
            JdbcManager.giveback(connection);
        }
    }
}
