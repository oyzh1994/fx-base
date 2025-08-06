package cn.oyzh.fx.editor;

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
    MARKDOWN("MD"),
    MAKEFIL("MAKEFIL"),
    PYTHON("PYTHON"),
    JS("JAVASCRIPT"),
    JAVA("JAVA"),
    C("C"),
    CSHARP("C#"),
    CPLUSPLUS("C++"),
    RUST("RUST"),
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
    KOTLIN("KOTLIN"),
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
    ASSEMBLER("ASSEMBLER_x86"),
    ASSEMBLER_6502("ASSEMBLER_6502"),
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
}
