package cn.oyzh.fx.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author oyzh
 * @since 2024-09-25
 */
public class JdbcResultSet implements AutoCloseable {

    private final ResultSet resultSet;

    private final Statement statement;

    public JdbcResultSet(ResultSet resultSet, Statement statement) {
        this.resultSet = resultSet;
        this.statement = statement;
    }

    @Override
    public void close() throws SQLException {
        this.resultSet.close();
        this.statement.close();
    }

    public boolean next() throws SQLException {
        return this.resultSet.next();
    }

    public int getInt(int columnIndex) throws SQLException {
        return this.resultSet.getInt(columnIndex);
    }

    public long getLong(int columnIndex) throws SQLException {
        return this.resultSet.getLong(columnIndex);
    }

    public int findColumn(String columnLabel) throws SQLException {
        return this.resultSet.findColumn(columnLabel);
    }

    public boolean containsColumn(String columnLabel) {
        try {
            return this.resultSet.findColumn(columnLabel) >= 0;
        } catch (SQLException ignored) {
        }
        return false;
    }

    public Object getObject(String columnLabel) throws SQLException {
        return this.resultSet.getObject(columnLabel);
    }
}
