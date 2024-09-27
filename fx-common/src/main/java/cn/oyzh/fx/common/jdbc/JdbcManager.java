package cn.oyzh.fx.common.jdbc;

import cn.hutool.core.collection.CollUtil;
import lombok.experimental.UtilityClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2024-09-25
 */
@UtilityClass
public class JdbcManager {

    /**
     * 方言
     */
    public static JdbcDialect dialect;

    /**
     * 连接列表
     */
    private static final List<JdbcConn> CONNECTIONS = new CopyOnWriteArrayList<>();

    static {
        String dialect = System.getProperty(JdbcConst.DB_DIALECT);
        JdbcManager.dialect = JdbcDialect.valueOf(dialect);
    }

    /**
     * 获取连接
     *
     * @return Connection
     * @throws SQLException 异常
     */
    public static Connection takeoff() throws SQLException {
        for (JdbcConn connection : CONNECTIONS) {
            if (connection.isUsable()) {
                return connection.takeoff();
            }
        }
        String dbFile = System.getProperty(JdbcConst.DB_FILE);
        Connection connection;
        if (dialect == JdbcDialect.H2) {
            connection = DriverManager.getConnection("jdbc:h2:" + dbFile);
        } else {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        }
        CONNECTIONS.add(new JdbcConn(connection));
        return connection;
    }

    /**
     * 归还连接
     *
     * @param connection 连接
     */
    public static void giveback(Connection connection) {
        if (connection != null) {
            List<JdbcConn> invalid = null;
            for (JdbcConn sqlConnection : CONNECTIONS) {
                if (sqlConnection.isInvalid()) {
                    if (invalid == null) {
                        invalid = new ArrayList<>();
                    }
                    invalid.add(sqlConnection);
                }
                if (sqlConnection.getConnection() == connection) {
                    sqlConnection.giveback();
                    break;
                }
            }
            if (CollUtil.isNotEmpty(invalid)) {
                CONNECTIONS.removeAll(invalid);
            }
        }
    }
}
