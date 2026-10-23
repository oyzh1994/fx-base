package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.fx.editor.incubator.EditorUtil;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.theme.Themes;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 编辑器主题解析测试，确保主题文件存在且明暗模式严格匹配。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public class EditorThemeResolutionTest {

    @Test
    public void resolveThemeResources() throws Exception {
        for (ThemeStyle style : Themes.themes()) {
            String resource = EditorUtil.resolveThemeResource(style);
            assertNotNull("theme resource is null: " + style.getName(), resource);
            // 解析结果应稳定
            assertEquals("theme resource is not stable: " + style.getName(),
                    resource, EditorUtil.resolveThemeResource(style));
            try (InputStream stream = EditorUtil.class.getResourceAsStream(resource)) {
                assertNotNull("theme resource not found: " + resource + " for " + style.getName(), stream);
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                String type = JSONUtil.parseObject(text).getString("type");
                // 暗色/亮色必须严格匹配
                assertEquals("theme mode mismatch: " + style.getName() + " -> " + resource,
                        style.isDarkMode(), "dark".equalsIgnoreCase(type));
            }
        }
    }
}
