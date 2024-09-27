package cn.oyzh.fx.common.jdbc;

import cn.hutool.core.util.ArrayUtil;
import cn.oyzh.fx.common.log.JulLog;
import lombok.experimental.UtilityClass;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * @author oyzh
 * @since 2024-09-25
 */
@UtilityClass
public class JdbcHelper {

    public static void execute(Connection connection, String sql) throws SQLException {
        JulLog.info(sql);
        Statement statement = connection.createStatement();
        statement.execute(sql);
        statement.close();
    }

    public static int executeUpdate(Connection connection, String sql, Collection<?> collection) throws SQLException {
        return executeUpdate(connection, sql, collection.toArray());
    }

    public static int executeUpdate(Connection connection, String sql, Object... params) throws SQLException {
        JulLog.info(sql);
        PreparedStatement statement = connection.prepareStatement(sql);
        setParams(statement, params);
        int update = statement.executeUpdate();
        statement.close();
        return update;
    }

    public static JdbcResultSet executeQuery(Connection connection, String sql, Collection<?> collection) throws SQLException {
        return executeQuery(connection, sql, collection.toArray());
    }

    public static JdbcResultSet executeQuery(Connection connection, String sql, Object... params) throws SQLException {
        JulLog.info(sql);
        if (ArrayUtil.isNotEmpty(params)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            setParams(statement, params);
            ResultSet resultSet = statement.executeQuery();
            return new JdbcResultSet(resultSet, statement);
        }
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        return new JdbcResultSet(resultSet, statement);
    }

    public static void setParams(PreparedStatement statement, Object... params) throws SQLException {
        if (ArrayUtil.isNotEmpty(params)) {
            int index = 1;
            for (Object param : params) {
                if (param == null) {
                    statement.setNull(index++, JDBCType.NULL.ordinal());
                } else {
                    statement.setObject(index++, param);
                }
            }
        }
    }
}
