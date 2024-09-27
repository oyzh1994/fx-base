package cn.oyzh.fx.common.test;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author oyzh
 * @since 2024-09-23
 */
public class H2Test {

    @Test
    public void test1() throws SQLException {
        Connection connection = null;
        try {
            String file = "d://test_h2";
            // 连接SQLite数据库文件，如果文件不存在，会自动创建
            connection = DriverManager.getConnection("jdbc:h2:" + file);
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (`id` INTEGER PRIMARY KEY, name TEXT, email TEXT)");
            statement.executeUpdate("INSERT INTO users (id, email) VALUES (1, 'alice@example.com')");
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    // 关闭连接
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void test2() throws SQLException {
        Connection connection = null;
        try {
            String file = "d://test.db";
            // 连接SQLite数据库文件，如果文件不存在，会自动创建
            connection = DriverManager.getConnection("jdbc:sqlite:" + file);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            while (resultSet.next()) {
                System.out.println(resultSet.getObject(1));
                System.out.println(resultSet.getObject(2));
            }
            resultSet.close();
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    // 关闭连接
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
