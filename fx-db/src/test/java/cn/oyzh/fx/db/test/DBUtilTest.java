package cn.oyzh.fx.db.test;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import org.junit.Test;

/**
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
}
