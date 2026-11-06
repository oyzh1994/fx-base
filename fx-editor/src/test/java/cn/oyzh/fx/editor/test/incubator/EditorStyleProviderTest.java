package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.fx.editor.incubator.EditorStyleProvider;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.junit.Test;
import tm4java.grammar.IGrammarSource;
import tm4java.theme.IThemeSource;
import tm4javafx.richtext.StyledToken;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 聊天语法配色测试。
 * <p>
 * 验证链接、文件路径、命令和代码在主题之上有稳定且可辨识的配色，
 * 避免主题只给链接加下划线（或不支持链接作用域）时看不出来。
 *
 * @author oyzh
 * @since 2026-11-06
 */
public class EditorStyleProviderTest {

    /**
     * 聊天语法
     */
    private static final String GRAMMAR = "/tm4javafx/grammars/llm-chat.tmLanguage.json";

    /**
     * 浅色主题
     */
    private static final String LIGHT_THEME = "/tm4javafx/themes/github-light-default.json";

    /**
     * 深色主题
     */
    private static final String DARK_THEME = "/tm4javafx/themes/github-dark-default.json";

    @Test
    public void lightThemeColors() {
        EditorStyleProvider provider = this.provider(LIGHT_THEME);
        List<StyledToken> tokens = provider.tokenize(
                "访问 https://example.com/api 和 /var/log/nginx/error.log，执行 `systemctl restart nginx`");

        StyledToken link = find(tokens, "https://example.com/api");
        assertNotNull(link);
        assertEquals(Color.web("#0969DA"), textColor(link));
        assertTrue("链接应带下划线", Boolean.TRUE.equals(link.style().get(StyleAttributeMap.UNDERLINE)));

        StyledToken path = find(tokens, "/var/log/nginx/error.log");
        assertNotNull(path);
        assertEquals(Color.web("#8250DF"), textColor(path));

        StyledToken code = find(tokens, "systemctl restart nginx");
        assertNotNull(code);
        assertEquals(Color.web("#116329"), textColor(code));

        // 普通文本沿用主题前景色
        StyledToken plain = find(tokens, "访问 ");
        assertNotNull(plain);
        assertEquals(Color.web("#1F2328"), textColor(plain));
    }

    @Test
    public void darkThemeColors() {
        EditorStyleProvider provider = this.provider(DARK_THEME);
        List<StyledToken> tokens = provider.tokenize("访问 https://example.com/api 和 /var/log/nginx/error.log");

        StyledToken link = find(tokens, "https://example.com/api");
        assertNotNull(link);
        assertEquals(Color.web("#58A6FF"), textColor(link));
        assertTrue("链接应带下划线", Boolean.TRUE.equals(link.style().get(StyleAttributeMap.UNDERLINE)));

        StyledToken path = find(tokens, "/var/log/nginx/error.log");
        assertNotNull(path);
        assertEquals(Color.web("#D2A8FF"), textColor(path));
    }

    @Test
    public void schemeLessLinkAndMarkdownLink() {
        EditorStyleProvider provider = this.provider(LIGHT_THEME);
        List<StyledToken> tokens = provider.tokenize(
                "看 github.com/org/repo 和 [文档](https://example.com/doc) 以及 www.example.com");

        StyledToken bare = find(tokens, "github.com/org/repo");
        assertNotNull(bare);
        assertEquals(Color.web("#0969DA"), textColor(bare));
        assertTrue(Boolean.TRUE.equals(bare.style().get(StyleAttributeMap.UNDERLINE)));

        StyledToken title = find(tokens, "文档");
        assertNotNull(title);
        assertEquals(Color.web("#0969DA"), textColor(title));
        assertTrue(Boolean.TRUE.equals(title.style().get(StyleAttributeMap.UNDERLINE)));

        StyledToken www = find(tokens, "www.example.com");
        assertNotNull(www);
        assertEquals(Color.web("#0969DA"), textColor(www));
    }

    @Test
    public void dangerousCommandIsRed() {
        EditorStyleProvider provider = this.provider(LIGHT_THEME);
        List<StyledToken> tokens = provider.tokenize("不要执行 sudo rm -rf /");
        StyledToken danger = find(tokens, "sudo rm -rf");
        assertNotNull(danger);
        assertEquals(Color.web("#CF222E"), textColor(danger));
    }

    @Test
    public void logLevelsAndStacktrace() {
        EditorStyleProvider provider = this.provider(LIGHT_THEME);

        List<StyledToken> tokens = provider.tokenize(
                "2026-11-06 23:15:22.456 ERROR [main] c.e.Foo NullPointerException: failed");
        assertEquals(Color.web("#6E7781"), textColor(find(tokens, "2026-11-06")));
        assertEquals(Color.web("#CF222E"), textColor(find(tokens, "ERROR")));
        assertEquals(Color.web("#6E7781"), textColor(find(tokens, "[main]")));
        assertEquals(Color.web("#953800"), textColor(find(tokens, "NullPointerException")));

        tokens = provider.tokenize("2026-11-06 07:15:22.456 WARN [pool-1-thread-2] ok");
        assertEquals(Color.web("#9A6700"), textColor(find(tokens, "WARN")));

        tokens = provider.tokenize("2026-11-06 07:15:22.456 INFO ok");
        assertEquals(Color.web("#0550AE"), textColor(find(tokens, "INFO")));

        tokens = provider.tokenize("\tat com.example.Foo.handle(Foo.java:123)");
        assertEquals(Color.web("#8250DF"), textColor(find(tokens, "Foo.java")));
        assertEquals(Color.web("#0550AE"), textColor(find(tokens, "123")));
    }

    @Test
    public void accessLogStatus() {
        EditorStyleProvider provider = this.provider(LIGHT_THEME);

        List<StyledToken> tokens = provider.tokenize(
                "192.168.1.10 - - [06/Nov/2026:23:15:22 +0800] \"GET /api HTTP/1.1\" 500 1234 \"-\" \"curl/8.0\"");
        assertEquals(Color.web("#CF222E"), textColor(find(tokens, "500")));

        tokens = provider.tokenize(
                "192.168.1.10 - - [06/Nov/2026:23:15:22 +0800] \"GET /api HTTP/1.1\" 200 1234 \"-\" \"curl/8.0\"");
        assertEquals(Color.web("#116329"), textColor(find(tokens, "200")));
    }

    @Test
    public void codeBlockColors() {
        EditorStyleProvider provider = this.provider(LIGHT_THEME);
        provider.tokenize("```java");
        assertEquals(Color.web("#6E7781"), textColor(find(provider.tokenize("// 注释"), "// 注释")));
        List<StyledToken> tokens = provider.tokenize(
                "public class Foo { String s = \"abc\"; Integer n = 1; }");
        assertEquals(Color.web("#CF222E"), textColor(find(tokens, "public")));
        assertEquals(Color.web("#953800"), textColor(find(tokens, "Foo")));
        assertEquals(Color.web("#0A3069"), textColor(find(tokens, "abc")));
        assertEquals(Color.web("#1F2328"), textColor(find(tokens, "{")));
        provider.tokenize("```");

        // JSON 键名
        tokens = provider.tokenize("{\"name\":\"nginx\",\"ok\":true}");
        assertEquals(Color.web("#953800"), textColor(find(tokens, "name")));
        assertEquals(Color.web("#CF222E"), textColor(find(tokens, "true")));
    }

    @Test
    public void markdownEmphasisAndHeading() {
        EditorStyleProvider provider = this.provider(LIGHT_THEME);

        StyledToken boldItalic = find(provider.tokenize("***粗斜体***"), "粗斜体");
        assertNotNull(boldItalic);
        assertTrue(Boolean.TRUE.equals(boldItalic.style().get(StyleAttributeMap.BOLD)));
        assertTrue(Boolean.TRUE.equals(boldItalic.style().get(StyleAttributeMap.ITALIC)));

        StyledToken heading = find(provider.tokenize("# 部署说明"), "部署说明");
        assertNotNull(heading);
        assertEquals(Color.web("#0550AE"), textColor(heading));
        assertTrue(Boolean.TRUE.equals(heading.style().get(StyleAttributeMap.BOLD)));
    }

    /**
     * 构造指定主题的样式提供者
     */
    private EditorStyleProvider provider(String theme) {
        EditorStyleProvider provider = new EditorStyleProvider();
        provider.setGrammar(IGrammarSource.fromResource(EditorStyleProviderTest.class, GRAMMAR));
        provider.setTheme(IThemeSource.fromResource(EditorStyleProviderTest.class, theme));
        return provider;
    }

    /**
     * 获取文本颜色
     */
    private static Color textColor(StyledToken token) {
        assertNotNull("token样式不能为空", token.style());
        return token.style().get(StyleAttributeMap.TEXT_COLOR);
    }

    /**
     * 按文本查找token
     */
    private static StyledToken find(List<StyledToken> tokens, String text) {
        for (StyledToken token : tokens) {
            if (token.text().contains(text)) {
                return token;
            }
        }
        return null;
    }
}
