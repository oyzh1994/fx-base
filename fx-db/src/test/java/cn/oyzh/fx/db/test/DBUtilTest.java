package cn.oyzh.fx.db.test;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBDataUtil;
import cn.oyzh.fx.db.util.DBUtil;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 数据库方言数据包装工具（DBUtil）测试
 *
 * @author oyzh
 * @since 2026-09-07
 */
public class DBUtilTest {

    @Test
    public void unwrapDataOnlyRemovesMatchingWrapperQuotes() {
        assertEquals("$.\"row1\".\"name\"",
                DBUtil.unwrapData("$.\"row1\".\"name\"", DBDialect.POSTGRESQL));
        assertEquals("wrapped", DBUtil.unwrapData("'wrapped'", DBDialect.POSTGRESQL));
        assertEquals("wrapped", DBUtil.unwrapData("\"wrapped\"", DBDialect.POSTGRESQL));
        assertEquals("'mismatch\"", DBUtil.unwrapData("'mismatch\"", DBDialect.POSTGRESQL));
    }

    @Test
    public void test1() {
        String str = "1:总人数 2:实际参会人数'";
        System.out.println(DBUtil.wrapData(str, DBDialect.DAMENG));
    }

    @Test
    public void wrapsRawStringLiterals() {
        assertEquals("'a''b'", DBUtil.wrapString("a'b", DBDialect.ORACLE));
        assertEquals("''''", DBUtil.wrapString("'", DBDialect.ORACLE));
        assertEquals("'''abc'''", DBUtil.wrapString("'abc'", DBDialect.ORACLE));
        assertEquals("''", DBUtil.wrapString("", DBDialect.ORACLE));
        assertEquals("'a\\b'", DBUtil.wrapString("a\\b", DBDialect.ORACLE));
        assertNull(DBUtil.wrapString(null, DBDialect.ORACLE));
    }

    @Test
    public void escapesMysqlLineBreaks() {
        assertEquals("'row-1\\nsecond line\\r'",
                DBUtil.wrapString("row-1\nsecond line\r", DBDialect.MYSQL));
        assertEquals("'row-1\\nsecond line'",
                DBUtil.wrapString("row-1\nsecond line", DBDialect.MARIADB));
        assertEquals("'row-10 \\\\slash ''quote''\\nsecond line\t😀'",
                DBUtil.wrapString("row-10 \\slash 'quote'\nsecond line\t😀", DBDialect.MYSQL));
    }

    @Test
    public void preservesQuotedDefaultsWithoutSingleQuoteCrash() {
        assertEquals("'N/A'", DBUtil.wrapData("'N/A'", DBDialect.ORACLE));
        assertEquals("''''", DBUtil.wrapData("'", DBDialect.ORACLE));
        assertEquals("''", DBDataUtil.escapeQuotes("'", DBDialect.ORACLE));
    }

    @Test
    public void testSetNullUsesJdbcType() throws Exception {
        AtomicInteger parameterIndex = new AtomicInteger();
        AtomicInteger sqlType = new AtomicInteger();
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("setNull".equals(method.getName())) {
                        parameterIndex.set((Integer) args[0]);
                        sqlType.set((Integer) args[1]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        DBUtil.setVal(statement, null, 2);

        assertEquals(2, parameterIndex.get());
        assertEquals(Types.NULL, sqlType.get());
    }
}
