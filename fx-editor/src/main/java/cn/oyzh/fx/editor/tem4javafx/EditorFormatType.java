package cn.oyzh.fx.editor.tem4javafx;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.i18n.I18nHelper;

/**
 * 富文本数据类型
 *
 * @author oyzh
 * @since 2024/5/17
 */
public enum EditorFormatType {
    RAW("RAW"),
    JSON("JSON"),
    XML("XML"),
    HTML("HTML"),
    YAML("YAML"),
    CSS("CSS"),
    PROPERTIES("PROPERTIES"),
    SH("SHELL"),
    BAT("BAT"),
    SQL("SQL"),
    DOCKERFILE("DOCKERFILE"),
    MD("MARKDOWN"),
    MAKEFIL("MAKEFIL"),
    PY("PYTHON"),
    JS("JAVASCRIPT"),
    JAVA("JAVA"),
    C("C"),
    CSHARP("C#"),
    CPLUSPLUS("C++"),
    RS("RUST"),
    RUBY("RUBY"),
    GO("GOLANG"),
    TS("TYPESCRIPT"),
    SCALA("SCALA"),
    PHP("PHP"),
    LUA("LUA"),
    LISP("LISP"),
    ACTIONSCRIPT("ACTIONSCRIPT"),
    DART("DART"),
    DELPHI("DELPHI"),
    D("D"),
    PERL("PERL"),
    FORTRAN("FORTRAN"),
    GROOVY("GROOVY"),
    HANDLEBARS("HANDLEBARS"),
    JSP("JSP"),
    KT("KOTLIN"),
    SAS("SAS"),
    CSV("CSV"),
    INI("INI"),
    VB("VB"),
    DTD("DTD"),
    LESS("LESS"),
    MXML("MXML"),
    NSIS("NSIS"),
    HOSTS("HOSTS"),
    HTACCESS("HTACCESS"),
    PROTO("PROTO"),
    ASM("ASSEMBLER_x86"),
    ASM_6502("ASSEMBLER_6502"),
    BBCODE("BBCODE"),
    CLOJURE("CLOJURE"),
    JSON_WITH_COMMENTS("JSON(" + I18nHelper.comment() + ")"),
    LATEX("LATEX"),
    TCL("TCL"),
    VHDL("VHDL");

    private final String name;

    public String getName() {
        return name;
    }

    EditorFormatType(String name) {
        this.name = name;
    }

    /**
     * 从名称获取类型
     *
     * @param name 名称
     * @return 类型
     */
    public static EditorFormatType ofName(String name) {
        if (StringUtil.equalsAnyIgnoreCase(name, "cs", "c#")) {
            return CSHARP;
        }
        if (StringUtil.equalsAnyIgnoreCase(name, "cpp", "c++")) {
            return CPLUSPLUS;
        }
        if (StringUtil.isNotBlank(name)) {
            for (EditorFormatType formatType : EditorFormatType.values()) {
                if (StringUtil.equalsIgnoreCase(formatType.name, name)) {
                    return formatType;
                }
                if (StringUtil.equalsIgnoreCase(formatType.toString(), name)) {
                    return formatType;
                }
            }
        }
        return RAW;
    }

}
