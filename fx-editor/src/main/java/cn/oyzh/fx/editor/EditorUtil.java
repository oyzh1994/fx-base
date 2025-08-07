package cn.oyzh.fx.editor;

import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

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
            case SH -> SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case BAT -> SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
            case SQL -> SyntaxConstants.SYNTAX_STYLE_SQL;
            case DOCKERFILE -> SyntaxConstants.SYNTAX_STYLE_DOCKERFILE;
            case MD -> SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
            case MAKEFIL -> SyntaxConstants.SYNTAX_STYLE_MAKEFILE;
            case PY -> SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case JS -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case JAVA -> SyntaxConstants.SYNTAX_STYLE_JAVA;
            case C -> SyntaxConstants.SYNTAX_STYLE_C;
            case CSHARP -> SyntaxConstants.SYNTAX_STYLE_CSHARP;
            case CPLUSPLUS -> SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
            case RS -> SyntaxConstants.SYNTAX_STYLE_RUST;
            case RUBY -> SyntaxConstants.SYNTAX_STYLE_RUBY;
            case GO -> SyntaxConstants.SYNTAX_STYLE_GO;
            case TS -> SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
            case SCALA -> SyntaxConstants.SYNTAX_STYLE_SCALA;
            case PHP -> SyntaxConstants.SYNTAX_STYLE_PHP;
            case LUA -> SyntaxConstants.SYNTAX_STYLE_LUA;
            case LISP -> SyntaxConstants.SYNTAX_STYLE_LISP;
            case ACTIONSCRIPT -> SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT;
            case DART -> SyntaxConstants.SYNTAX_STYLE_DART;
            case DELPHI -> SyntaxConstants.SYNTAX_STYLE_DELPHI;
            case D -> SyntaxConstants.SYNTAX_STYLE_D;
            case PERL -> SyntaxConstants.SYNTAX_STYLE_PERL;
            case FORTRAN -> SyntaxConstants.SYNTAX_STYLE_FORTRAN;
            case GROOVY -> SyntaxConstants.SYNTAX_STYLE_GROOVY;
            case HANDLEBARS -> SyntaxConstants.SYNTAX_STYLE_HANDLEBARS;
            case JSP -> SyntaxConstants.SYNTAX_STYLE_JSP;
            case KT -> SyntaxConstants.SYNTAX_STYLE_KOTLIN;
            case SAS -> SyntaxConstants.SYNTAX_STYLE_SAS;
            case CSV -> SyntaxConstants.SYNTAX_STYLE_CSV;
            case INI -> SyntaxConstants.SYNTAX_STYLE_INI;
            case VB -> SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC;
            case DTD -> SyntaxConstants.SYNTAX_STYLE_DTD;
            case LESS -> SyntaxConstants.SYNTAX_STYLE_LESS;
            case MXML -> SyntaxConstants.SYNTAX_STYLE_MXML;
            case NSIS -> SyntaxConstants.SYNTAX_STYLE_NSIS;
            case HOSTS -> SyntaxConstants.SYNTAX_STYLE_HOSTS;
            case HTACCESS -> SyntaxConstants.SYNTAX_STYLE_HTACCESS;
            case PROTO -> SyntaxConstants.SYNTAX_STYLE_PROTO;
            case ASM -> SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86;
            case ASM_6502 -> SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_6502;
            case BBCODE -> SyntaxConstants.SYNTAX_STYLE_BBCODE;
            case CLOJURE -> SyntaxConstants.SYNTAX_STYLE_CLOJURE;
            case JSON_WITH_COMMENTS -> SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
            case LATEX -> SyntaxConstants.SYNTAX_STYLE_LATEX;
            case TCL -> SyntaxConstants.SYNTAX_STYLE_TCL;
            case VHDL -> SyntaxConstants.SYNTAX_STYLE_VHDL;
            default -> SyntaxConstants.SYNTAX_STYLE_NONE;
        };
    }

    // /**
    //  * 获取光标宽度（通常是1-2像素）
    //  *
    //  * @param textArea 文本域
    //  * @return 结果
    //  */
    // public static int getCaretWidth(RSyntaxTextArea textArea) {
    //     // Swing光标宽度通常由UI决定，默认1像素
    //     // 可通过Caret的paint方法推断，这里返回常见值
    //     return 1; // 大多数外观下的默认值
    // }
    //
    // /**
    //  * 获取光标高度（基于当前字体）
    //  *
    //  * @param textArea 文本域
    //  * @return 结果
    //  */
    // public static int getCaretHeight(RSyntaxTextArea textArea) {
    //     // 获取当前字体的Metrics
    //     FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
    //     // 光标高度约等于字体的 ascent + descent（字符总高度）
    //     return metrics.getAscent() + metrics.getDescent();
    // }

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
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
                } else if (ThemeManager.currentTheme() == Themes.CUPERTINO_LIGHT) {
                    // stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml");
                    // stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml");
                } else {
                    stream = ResourceUtil.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml");
                }
            }
            Theme theme = Theme.load(stream);
            theme.apply(textArea);
        } catch (Exception ignored) {
        }
    }
}
