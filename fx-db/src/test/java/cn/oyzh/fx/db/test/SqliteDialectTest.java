package cn.oyzh.fx.db.test;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.sql.DBBaseSqlParser;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.db.util.DBDataUtil;
import cn.oyzh.fx.db.util.DBUtil;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 通用方言测试。
 *
 * @since 2026-11-02
 */
public class SqliteDialectTest {

    @Test
    public void testDialectAndSql() {
        assertTrue(DBDialect.valueList().contains(DBDialect.SQLITE));
        assertEquals("\"order\"", DBUtil.wrap("order", DBDialect.SQLITE));
        assertEquals("\"a\"\"b\"", DBUtil.wrap("a\"b", DBDialect.SQLITE));
        assertEquals("a''b", DBDataUtil.escapeQuotes("a'b", DBDialect.SQLITE));
        assertEquals("'a''b'", DBUtil.wrapData("a'b", DBDialect.SQLITE));

        List<String> sqlList = DBSqlParser.parseSql("SELECT 1; SELECT 2", DBDialect.SQLITE);
        assertEquals(2, sqlList.size());
        assertEquals(3, DBSqlParser.parseSql("BEGIN; INSERT INTO item VALUES('a;b'); COMMIT;", DBDialect.SQLITE).size());
        String trigger = "CREATE TRIGGER item_insert AFTER INSERT ON item BEGIN "
                + "INSERT INTO log(message) VALUES('created;item'); UPDATE log SET count = CASE WHEN count > 0 THEN count + 1 ELSE 1 END; "
                + "END; SELECT 1;";
        List<String> triggerSql = DBSqlParser.parseSql(trigger, DBDialect.SQLITE);
        assertEquals(2, triggerSql.size());
        assertTrue(triggerSql.getFirst().contains("CASE WHEN"));
        assertEquals("SELECT 1", triggerSql.get(1));
        assertEquals(1, DBSqlParser.parseSql(
                "CREATE TEMP TRIGGER trg AFTER DELETE ON item BEGIN DELETE FROM log; END", DBDialect.SQLITE).size());
        assertTrue(new DBBaseSqlParser("SELECT * FROM item", DBDialect.SQLITE).isSelect("SELECT * FROM item"));
    }

}
