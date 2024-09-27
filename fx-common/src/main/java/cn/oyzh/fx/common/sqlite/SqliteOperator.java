package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.date.DateUtil;
import cn.oyzh.fx.common.jdbc.ColumnDefinition;
import cn.oyzh.fx.common.jdbc.JdbcResultSet;
import cn.oyzh.fx.common.jdbc.JdbcHelper;
import cn.oyzh.fx.common.jdbc.PageParam;
import cn.oyzh.fx.common.jdbc.PrimaryKeyColumn;
import cn.oyzh.fx.common.jdbc.QueryParam;
import cn.oyzh.fx.common.jdbc.SelectListParam;
import cn.oyzh.fx.common.jdbc.JdbcManager;
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
public class SqliteOperator {

    @Getter
    private final TableDefinition tableDefinition;

    public SqliteOperator(TableDefinition tableDefinition) {
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
            String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql, tableName);
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

    protected void createTable() throws SQLException {
        Connection connection = JdbcManager.takeoff();
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

    public int insert(Map<String, Object> record) throws Exception {
        String tableName = this.tableName();
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
                sql.append(SqlLiteUtil.wrapData(entry.getValue()));
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
        sql.append(SqlLiteUtil.wrapData(param.getData()));
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
        if(CollUtil.isEmpty(param.getQueryColumns())){
            sql.append("*");
        }else{
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
                sql.append(SqlLiteUtil.wrapData(queryParam.getData()));
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
                sql.append(SqlLiteUtil.wrapData(param.getData()));
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
                sql.append(SqlLiteUtil.wrapData("%" + kw + "%"));
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
                sql.append(SqlLiteUtil.wrapData("%" + kw + "%"));
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
                sql.append(SqlLiteUtil.wrapData(entry.getValue()));
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
