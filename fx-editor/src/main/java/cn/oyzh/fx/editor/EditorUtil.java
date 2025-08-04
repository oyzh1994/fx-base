package cn.oyzh.fx.editor;

import cn.oyzh.fx.rich.RichTextStyle;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class EditorUtil {


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

}
