package cn.oyzh.fx.db.test;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.sql.DBBaseSqlParser;
import cn.oyzh.fx.db.sql.DBOraclePagination;
import cn.oyzh.fx.db.util.DBUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Oracle方言测试
 *
 * @author oyzh
 * @since 2026-11-18
 */
public class OracleDialectTest {

    @Test
    public void supportsOracleSqlParsingAndQuoting() {
        DBBaseSqlParser parser = new DBBaseSqlParser("SELECT 1 FROM DUAL", DBDialect.ORACLE);
        assertTrue(parser.isSelect("SELECT 1 FROM DUAL"));
        assertEquals("\"order\"", DBUtil.wrap("order", DBDialect.ORACLE));
    }

    @Test
    public void generatesCompatibleOraclePagination() {
        String legacy = DBOraclePagination.paginate("SELECT * FROM users", 20, 10,
                "Oracle Database 11g Enterprise Edition Release 11.2.0.4.0");
        assertTrue(legacy.contains("ROWNUM <= 30"));
        assertFalse(legacy.contains("OFFSET"));

        String modern = DBOraclePagination.paginate("SELECT * FROM users", 20, 10,
                "Oracle Database 12c Enterprise Edition Release 12.1.0.2.0");
        assertTrue(modern.endsWith("OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY"));
    }
}
