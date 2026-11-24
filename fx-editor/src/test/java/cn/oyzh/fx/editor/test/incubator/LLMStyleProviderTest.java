package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.fx.editor.incubator.control.LLMStyleProvider;
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
public class LLMStyleProviderTest {

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
        LLMStyleProvider provider = this.provider(LIGHT_THEME);
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
        LLMStyleProvider provider = this.provider(DARK_THEME);
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
        LLMStyleProvider provider = this.provider(LIGHT_THEME);
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

        StyledToken titled = find(provider.tokenize(
                "[文档](https://example.com/doc \"标题\")"), "https://example.com/doc");
        assertNotNull(titled);
        assertEquals(Color.web("#0969DA"), textColor(titled));
    }

    @Test
    public void dangerousCommandIsRed() {
        LLMStyleProvider provider = this.provider(LIGHT_THEME);
        List<StyledToken> tokens = provider.tokenize("不要执行 sudo rm -rf /");
        StyledToken danger = find(tokens, "sudo rm -rf");
        assertNotNull(danger);
        assertEquals(Color.web("#CF222E"), textColor(danger));
    }

    @Test
    public void logLevelsAndStacktrace() {
        LLMStyleProvider provider = this.provider(LIGHT_THEME);

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
        LLMStyleProvider provider = this.provider(LIGHT_THEME);

        List<StyledToken> tokens = provider.tokenize(
                "192.168.1.10 - - [06/Nov/2026:23:15:22 +0800] \"GET /api HTTP/1.1\" 500 1234 \"-\" \"curl/8.0\"");
        assertEquals(Color.web("#CF222E"), textColor(find(tokens, "500")));

        tokens = provider.tokenize(
                "192.168.1.10 - - [06/Nov/2026:23:15:22 +0800] \"GET /api HTTP/1.1\" 200 1234 \"-\" \"curl/8.0\"");
        assertEquals(Color.web("#116329"), textColor(find(tokens, "200")));
    }

    @Test
    public void codeBlockColors() {
        LLMStyleProvider provider = this.provider(LIGHT_THEME);
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
        LLMStyleProvider provider = this.provider(LIGHT_THEME);

        StyledToken boldItalic = find(provider.tokenize("***粗斜体***"), "粗斜体");
        assertNotNull(boldItalic);
        assertTrue(Boolean.TRUE.equals(boldItalic.style().get(StyleAttributeMap.BOLD)));
        assertTrue(Boolean.TRUE.equals(boldItalic.style().get(StyleAttributeMap.ITALIC)));

        StyledToken heading = find(provider.tokenize("# 部署说明"), "部署说明");
        assertNotNull(heading);
        assertEquals(Color.web("#0550AE"), textColor(heading));
        assertTrue(Boolean.TRUE.equals(heading.style().get(StyleAttributeMap.BOLD)));
    }

    @Test
    public void markdownEmphasisVariants() {
        LLMStyleProvider provider = this.provider(LIGHT_THEME);

        StyledToken bold = find(provider.tokenize("__粗体__"), "粗体");
        assertNotNull(bold);
        assertTrue(Boolean.TRUE.equals(bold.style().get(StyleAttributeMap.BOLD)));

        StyledToken italic = find(provider.tokenize("_斜体_"), "斜体");
        assertNotNull(italic);
        assertTrue(Boolean.TRUE.equals(italic.style().get(StyleAttributeMap.ITALIC)));

        StyledToken nested = find(provider.tokenize("**粗体 _斜体_**"), "斜体");
        assertNotNull(nested);
        assertTrue(Boolean.TRUE.equals(nested.style().get(StyleAttributeMap.ITALIC)));

        StyledToken strike = find(provider.tokenize("~~删除~~"), "删除");
        assertNotNull(strike);
        assertTrue(Boolean.TRUE.equals(strike.style().get(StyleAttributeMap.STRIKE_THROUGH)));
        assertEquals(Color.web("#6E7781"), textColor(strike));
    }

    @Test
    public void markdownInlineCodeWithNestedBackticks() {
        LLMStyleProvider provider = this.provider(LIGHT_THEME);
        List<StyledToken> tokens = provider.tokenize("使用 ``a`b`` 代码");

        assertEquals(Color.web("#116329"), textColor(find(tokens, "a")));
        assertEquals(Color.web("#116329"), textColor(find(tokens, "b")));
        assertEquals(Color.web("#1F2328"), textColor(find(tokens, " 代码")));
    }

    @Test
    public void markdownTableAndHtml() {
        LLMStyleProvider provider = this.provider(LIGHT_THEME);

        assertEquals(Color.web("#1F2328"), textColor(find(provider.tokenize("| 名称 | 状态 |"), "名称")));
        assertEquals(Color.web("#6E7781"), textColor(find(provider.tokenize("| --- | :---: |"), "---")));

        StyledToken tableBold = find(provider.tokenize("| 1 | **正常** |"), "正常");
        assertNotNull(tableBold);
        assertTrue(Boolean.TRUE.equals(tableBold.style().get(StyleAttributeMap.BOLD)));

        assertEquals(Color.web("#953800"), textColor(find(provider.tokenize("<details><summary>说明</summary>"), "details")));
        assertEquals(Color.web("#116329"), textColor(find(provider.tokenize("A&nbsp;B"), "&nbsp;")));
    }

    @Test
    public void markdownTaskListState() {
        LLMStyleProvider provider = this.provider(LIGHT_THEME);

        assertEquals(Color.web("#116329"), textColor(find(provider.tokenize("- [x] 完成"), "[x]")));
        assertEquals(Color.web("#9A6700"), textColor(find(provider.tokenize("- [ ] 待办"), "[ ]")));
    }

    @Test
    public void commandResultMarkers() {
        LLMStyleProvider provider = this.provider(LIGHT_THEME);

        assertEquals(Color.web("#116329"),
                textColor(find(provider.tokenize("✓ $ mvn -q -DskipTests compile"), "✓")));
        assertEquals(Color.web("#CF222E"),
                textColor(find(provider.tokenize("✗ $ curl https://example.com"), "✗")));
    }

    /**
     * 构造指定主题的样式提供者
     */
    private LLMStyleProvider provider(String theme) {
        LLMStyleProvider provider = new LLMStyleProvider();
        provider.setGrammar(IGrammarSource.fromResource(LLMStyleProviderTest.class, GRAMMAR));
        provider.setTheme(IThemeSource.fromResource(LLMStyleProviderTest.class, theme));
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
