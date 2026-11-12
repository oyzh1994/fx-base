package cn.oyzh.fx.db.test;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * 数据库方言数据包装工具（DBUtil）测试
 *
 * @author oyzh
 * @since 2026-09-07
 */
public class DBUtilTest {

    @Test
    public void test1() {
        String str = "1:总人数 2:实际参会人数'";
        System.out.println(DBUtil.wrapData(str, DBDialect.DAMENG));
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
