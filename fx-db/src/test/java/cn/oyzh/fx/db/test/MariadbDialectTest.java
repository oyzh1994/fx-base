package cn.oyzh.fx.db.test;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.sql.DBBaseSqlParser;
import cn.oyzh.fx.db.sql.DBDruidSqlParser;
import cn.oyzh.fx.db.util.DBDataUtil;
import cn.oyzh.fx.db.util.DBUtil;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * MariaDB方言测试
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbDialectTest {

    @Test
    public void testDistinctDialect() {
        assertNotEquals(DBDialect.MYSQL, DBDialect.MARIADB);
        assertTrue(DBDialect.valueList().contains(DBDialect.MARIADB));
    }

    @Test
    public void testMysqlCompatibleBusinessRules() {
        assertEquals("`order`", DBUtil.wrap("order", DBDialect.MARIADB));
        assertEquals("a''b", DBDataUtil.escapeQuotes("a'b", DBDialect.MARIADB));
        assertEquals("'a''b'", DBUtil.wrapData("a'b", DBDialect.MARIADB));
    }

    @Test
    public void testMariadbSqlParsers() {
        DBBaseSqlParser baseParser = new DBBaseSqlParser("SELECT 1; SELECT 2", DBDialect.MARIADB);
        List<String> sqlList = baseParser.parseSql();
        assertEquals(2, sqlList.size());
        assertTrue(baseParser.isSelect("SELECT 1"));
        assertTrue(baseParser.isFullColumn("SELECT * FROM items"));

        DBDruidSqlParser druidParser = new DBDruidSqlParser("SHOW CREATE EVENT sample_event", DBDialect.MARIADB);
        assertTrue(druidParser.isSingle());
        assertTrue(druidParser.isSelect("SHOW CREATE EVENT sample_event"));
    }
}
