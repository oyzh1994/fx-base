package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.fx.editor.incubator.EditorFormatType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 编辑器格式顺序与扩展名优先级测试。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public class EditorFormatTypeOrderTest {

    @Test
    public void orderAndExtension() {
        EditorFormatType[] values = EditorFormatType.values();
        assertEquals(125, values.length);
        // 常用格式在前
        assertEquals(EditorFormatType.RAW, values[0]);
        assertEquals(EditorFormatType.JSON, values[1]);
        int markdown = indexOf(EditorFormatType.MARKDOWN);
        int language = indexOf(EditorFormatType.JAVASCRIPT);
        int web = indexOf(EditorFormatType.SCSS);
        int internal = indexOf(EditorFormatType.SOURCE_SASSDOC);
        assertTrue("文本格式应在语言之前", markdown < language);
        assertTrue("语言应在Web模板之前", language < web);
        assertTrue("内部语法应在最后", web < internal);
        assertEquals(EditorFormatType.CPP_EMBEDDED_LATEX, values[values.length - 1]);
        // 扩展名冲突时保持常见格式优先
        assertEquals(EditorFormatType.CPP, EditorFormatType.ofExtension("cpp"));
        assertEquals(EditorFormatType.MARKDOWN, EditorFormatType.ofExtension("md"));
        assertEquals(EditorFormatType.DOTENV, EditorFormatType.ofExtension("env"));
        assertEquals(EditorFormatType.JSON, EditorFormatType.ofExtension("json"));
        assertEquals(EditorFormatType.HTML, EditorFormatType.ofExtension("html"));
    }

    private static int indexOf(EditorFormatType type) {
        EditorFormatType[] values = EditorFormatType.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == type) {
                return i;
            }
        }
        return -1;
    }

    @Test
    public void filterFormats() {
        assertEquals(EditorFormatType.values().length, EditorFormatType.filter(null).size());
        assertTrue(contains("json", EditorFormatType.JSON));
        assertTrue(contains("react", EditorFormatType.JAVASCRIPTREACT));
        assertTrue(contains("react", EditorFormatType.TYPESCRIPTREACT));
        assertTrue(contains("yml", EditorFormatType.YAML));
        assertTrue(contains("kt", EditorFormatType.KOTLIN));
        assertTrue(contains("py", EditorFormatType.PYTHON));
        assertTrue(contains("nginx", EditorFormatType.NGINX));
        assertFalse(contains("扩展名不存在", EditorFormatType.JSON));
        assertTrue(EditorFormatType.filter("not-exists-format").isEmpty());
    }

    private static boolean contains(String keyword, EditorFormatType type) {
        List<EditorFormatType> formats = EditorFormatType.filter(keyword);
        return formats.contains(type);
    }
}
