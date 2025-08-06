package cn.oyzh.fx.editor;

import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import java.awt.FontMetrics;
import java.io.InputStream;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class EditorUtil {

    /**
     * 转换为语法名称
     *
     * @param formatType 格式类型
     * @return 语法名称
     */
    public static String toSyntax(EditorFormatType formatType) {
        return switch (formatType) {
            case CSS -> SyntaxConstants.SYNTAX_STYLE_CSS;
            case XML -> SyntaxConstants.SYNTAX_STYLE_XML;
            case JSON -> SyntaxConstants.SYNTAX_STYLE_JSON;
            case YAML -> SyntaxConstants.SYNTAX_STYLE_YAML;
            case HTML -> SyntaxConstants.SYNTAX_STYLE_HTML;
            case PROPERTIES -> SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
            default -> SyntaxConstants.SYNTAX_STYLE_NONE;
        };
    }

    /**
     * 获取光标宽度（通常是1-2像素）
     *
     * @param textArea 文本域
     * @return 结果
     */
    public static int getCaretWidth(RSyntaxTextArea textArea) {
        // Swing光标宽度通常由UI决定，默认1像素
        // 可通过Caret的paint方法推断，这里返回常见值
        return 1; // 大多数外观下的默认值
    }

    /**
     * 获取光标高度（基于当前字体）
     *
     * @param textArea 文本域
     * @return 结果
     */
    public static int getCaretHeight(RSyntaxTextArea textArea) {
        // 获取当前字体的Metrics
        FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
        // 光标高度约等于字体的 ascent + descent（字符总高度）
        return metrics.getAscent() + metrics.getDescent();
    }

    /**
     * 应用样式
     *
     * @param textArea 文本域
     */
    public static void applyTheme(RSyntaxTextArea textArea) {
        if (textArea == null) {
            return;
        }
        try {
            InputStream stream;
            // 暗色
            if (ThemeManager.isDarkMode()) {
                if (ThemeManager.currentTheme() == Themes.DRACULA) {
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml");
                } else {
                    // stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/druid.xml");
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml");
                }
            } else {// 亮色
                if (ThemeManager.currentTheme() == Themes.PRIMER_LIGHT) {
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/vs.xml");
                } else if (ThemeManager.currentTheme() == Themes.NORD_LIGHT) {
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml");
                } else if (ThemeManager.currentTheme() == Themes.CUPERTINO_LIGHT) {
                    // stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml");
                    // stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml");
                } else {
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
                }
            }
            Theme theme = Theme.load(stream);
            theme.apply(textArea);
        } catch (Exception ignored) {
        }
    }
}
