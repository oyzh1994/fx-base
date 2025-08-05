package cn.oyzh.fx.editor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import java.awt.*;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class EditorUtil {

    public static final Color CURRENT_LINE_HIGHLIGHT_COLOR_DARK    = new Color(255, 100, 100);

    public static final Color CURRENT_LINE_HIGHLIGHT_COLOR_LIGHT = new Color(255, 255, 170);

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

    // 获取光标宽度（通常是1-2像素）
    public static int getCaretWidth(RSyntaxTextArea textArea) {
        // Swing光标宽度通常由UI决定，默认1像素
        // 可通过Caret的paint方法推断，这里返回常见值
        return 1; // 大多数外观下的默认值
    }

    // 获取光标高度（基于当前字体）
    public static int getCaretHeight(RSyntaxTextArea textArea) {
        // 获取当前字体的Metrics
        FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
        // 光标高度约等于字体的 ascent + descent（字符总高度）
        return metrics.getAscent() + metrics.getDescent();
    }
}
