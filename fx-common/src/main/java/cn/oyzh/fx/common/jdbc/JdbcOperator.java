package cn.oyzh.fx.common.jdbc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-23
 */
public abstract class JdbcOperator {

    @Getter
    protected final TableDefinition tableDefinition;

    public JdbcOperator(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    protected String tableName() {
        return this.tableDefinition.getTableName();
    }

    protected List<ColumnDefinition> columns() {
        return this.tableDefinition.getColumns();
    }

    protected PrimaryKeyColumn getPrimaryKeyColumn(Object primaryKey) {
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
        return false;
    }

    protected void alterTable() throws SQLException {
    }

    protected void createTable() throws SQLException {
    }

    public int insert(Map<String, Object> record) throws Exception {
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(JdbcUtil.wrap(tableName));
        sql.append("(");
        for (String column : record.keySet()) {
            sql.append(JdbcUtil.wrap(column)).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES(");
        sql.append("?,".repeat(record.size()));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        JdbcConn connection = JdbcManager.takeoff();
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
        sql.append(JdbcUtil.wrap(tableName));
        sql.append(" SET ");
        for (String column : record.keySet()) {
            sql.append(JdbcUtil.wrap(column)).append(" = ?,");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append(" = ?");
        JdbcConn connection = JdbcManager.takeoff();
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
        sql.append(JdbcUtil.wrap(tableName));
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append(" = ?");
        JdbcConn connection = JdbcManager.takeoff();
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
                sql.append(JdbcUtil.wrap(entry.getKey()));
                sql.append(" = ");
                sql.append(JdbcUtil.wrapData(entry.getValue()));
            }
        }
        JdbcConn connection = JdbcManager.takeoff();
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
        sql.append(JdbcUtil.wrap(tableName));
        sql.append(" WHERE ");
        sql.append(JdbcUtil.wrap(primaryKey.getColumnName()));
        sql.append(" = ?");
        JdbcConn connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString(), primaryKey.getColumnData());
            Map<String, Object> record = new HashMap<>();
            while (resultSet.next()) {
                for (ColumnDefinition columnDefinition : this.columns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.containsColumn(columnName)) {
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

    public Map<String, Object> selectOne(QueryParam queryParam) throws SQLException {
        SelectParam selectParam = new SelectParam();
        selectParam.addQueryParam(queryParam);
        return this.selectOne(selectParam);
    }

    public Map<String, Object> selectOne(SelectParam selectParam) throws SQLException {
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("SELECT ");
        if (CollUtil.isEmpty(selectParam.getQueryColumns())) {
            sql.append("*");
        } else {
            for (String queryColumn : selectParam.getQueryColumns()) {
                sql.append(queryColumn).append(",");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
        }
        sql.append(" FROM ");
        sql.append(JdbcUtil.wrap(tableName));
        if (CollUtil.isNotEmpty(selectParam.getQueryParams())) {
            boolean first = true;
            for (QueryParam queryParam : selectParam.getQueryParams()) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" AND ");
                }
                sql.append(JdbcUtil.wrap(queryParam.getName()));
                sql.append(queryParam.getOperator());
                sql.append(JdbcUtil.wrapData(queryParam.getData()));
            }
        }
        JdbcConn connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
            Map<String, Object> record = new HashMap<>();
            while (resultSet.next()) {
                for (ColumnDefinition columnDefinition : this.columns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.containsColumn(columnName)) {
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

    public List<Map<String, Object>> selectList(SelectParam param) throws SQLException {
        String tableName = this.tableName();
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
                sql.append(JdbcUtil.wrap(queryParam.getName()));
                sql.append(queryParam.getOperator());
                sql.append(JdbcUtil.wrapData(queryParam.getData()));
            }
        }
        JdbcConn connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
            List<Map<String, Object>> records = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> record = new HashMap<>();
                for (ColumnDefinition columnDefinition : this.columns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.containsColumn(columnName)) {
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
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(JdbcUtil.wrap(tableName));
        if (CollUtil.isNotEmpty(params)) {
            boolean first = true;
            for (QueryParam param : params) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" AND ");
                }
                sql.append(JdbcUtil.wrap(param.getName()));
                sql.append(param.getOperator());
                sql.append(JdbcUtil.wrapData(param.getData()));
            }
        }
        JdbcConn connection = JdbcManager.takeoff();
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
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(JdbcUtil.wrap(tableName));
        if (StrUtil.isNotBlank(kw) && CollUtil.isNotEmpty(columns)) {
            boolean first = true;
            for (String column : columns) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" OR ");
                }
                sql.append(JdbcUtil.wrap(column));
                sql.append(" LIKE ");
                sql.append(JdbcUtil.wrapData("%" + kw + "%"));
            }
        }
        JdbcConn connection = JdbcManager.takeoff();
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
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(JdbcUtil.wrap(tableName));
        if (StrUtil.isNotBlank(kw) && CollUtil.isNotEmpty(columns)) {
            boolean first = true;
            for (String column : columns) {
                if (first) {
                    first = false;
                    sql.append(" WHERE ");
                } else {
                    sql.append(" OR ");
                }
                sql.append(JdbcUtil.wrap(column));
                sql.append(" LIKE ");
                sql.append(JdbcUtil.wrapData("%" + kw + "%"));
            }
        }
        sql.append(" LIMIT ")
                .append(pageParam.getLimit())
                .append(" OFFSET ")
                .append(pageParam.getStart());
        JdbcConn connection = JdbcManager.takeoff();
        try {
            JdbcResultSet resultSet = JdbcHelper.executeQuery(connection, sql.toString());
            List<Map<String, Object>> records = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> record = new HashMap<>();
                for (ColumnDefinition columnDefinition : this.columns()) {
                    String columnName = columnDefinition.getColumnName();
                    if (resultSet.containsColumn(columnName)) {
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
        String tableName = this.tableName();
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(JdbcUtil.wrap(tableName));
        sql.append(" WHERE ");
        sql.append(primaryKey.getColumnName());
        sql.append(" = ?");
        JdbcConn connection = JdbcManager.takeoff();
        try {
            return JdbcHelper.executeUpdate(connection, sql.toString(), primaryKey.getColumnData());
        } finally {
            JdbcManager.giveback(connection);
        }
    }

    public abstract int delete(Map<String, Object> params, Long limit) throws SQLException;
}
