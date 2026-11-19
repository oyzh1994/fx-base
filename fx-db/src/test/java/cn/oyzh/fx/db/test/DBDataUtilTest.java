package cn.oyzh.fx.db.test;

import cn.oyzh.fx.db.util.DBDataUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * DBDataUtil JSON 规范化测试。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public class DBDataUtilTest {

    @Test
    public void normalizesEscapedJson() {
        assertEquals("{\"id\":1}", DBDataUtil.normalizeJson("  {\\\"id\\\":1}  "));
        assertEquals("[1,2,3]", DBDataUtil.normalizeJson(" [1,2,3] "));
        assertEquals("{\"id\":1}", DBDataUtil.normalizeJson("{\"id\":1}"));
        assertNull(DBDataUtil.normalizeJson(null));
    }

    @Test
    public void keepsNonJsonEscapedTextUnchanged() {
        assertEquals("a\\nb", DBDataUtil.normalizeJson("a\\nb"));
        assertEquals("{\\\"id\\\":}", DBDataUtil.normalizeJson("{\\\"id\\\":}"));
    }
}
