package cn.oyzh.fx.editor.tm4javafx;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.i18n.I18nHelper;

/**
 * 富文本数据类型
 *
 * @author oyzh
 * @since 2024/5/17
 */
public enum EditorFormatType {
    RAW("RAW", ""),
    // 常见文件
    JSON("JSON", "json"),
    JSONC("JSONC", "jsonc"),
    JSONL("JSONL", "jsonl"),
    XML("XML", "xml"),
    HTML("HTML", "html"),
    YAML("YAML", "yaml"),
    CSS("CSS", "css"),
    SHELLSCRIPT("SHELL SCRIPT", "sh"),
    BAT("BAT", "bat"),
    SQL("SQL", "sql"),
    DOCKERFILE("DOCKERFILE", "dockerfile"),
    MAKEFILE("MAKEFILE", "makefile"),
    MARKDOWN("MARKDOWN", "md"),
    LOG("LOG", "log"),
    // 语言
    C("C", "c"),
    JAVASCRIPT("JAVASCRIPT", "js"),
    JAVASCRIPTREACT("JAVASCRIPT REACT", "jsx"),
    PYTHON("PYTHON", "py"),
    JAVA("JAVA", "java"),
    CSHARP("C#", "cs"),
    CPP("C++", "cpp"),
    CUDA_CPP("CUDA CPP", "cu"),
    RUST("RUST", "rs"),
    RUBY("RUBY", "rb"),
    GO("GO", "go"),
    TYPESCRIPT("TYPESCRIPT", "ts"),
    TYPESCRIPTREACT("TYPESCRIPT REACT", "tsx"),
    PHP("PHP", "php"),
    LUA("LUA", "lua"),
    DART("DART", "dart"),
    PERL("PERL", "pl"),
    GROOVY("GROOVY", "groovy"),
    COFFEESCRIPT("COFFEESCRIPT", "coffee"),
    HANDLEBARS("HANDLEBARS", "hbs"),
    JSP("JSP", "jsp"),
    KT("KOTLIN", "kt"),
    R("R", "r"),
    SWIFT("SWIFT", "swift"),
    OBJECTIVE_C("OBJECTIVE C", "m"),
    OBJECTIVE_CPP("OBJECTIVE CPP", "mm"),
    FSHARP("FSHARP", "fs"),
    JULIA("JULIA", "julia"),
    CLOJURE("CLOJURE", "clj"),
    CPP_EMBEDDED_LATEX("CPP EMBEDDED LATEX", "cpp"),
    // 配置文件
    CSV("CSV", "csv"),
    INI("INI", "ini"),
    VB("VB", "vb"),
    XLS("XLS", "xls"),
    LESS("LESS", "less"),
    POWERSHELL("POWERSHELL", "ps1"),
    IGNORE("IGNORE", "ignore"),
    LATEX("LATEX", "ltx"),
    SCSS("SCSS", "scss"),
    JADE("JADE", "pug"),
    TEX("TEX", "tex"),
    GIT_COMMIT("GIT COMMIT", "txt"),
    GIT_REBASE("GIT REBASE", "git-rebase-todo"),
    BIBTEX("BIBTEX", "bib"),
    DIFF("DIFF", "diff"),
    HLSL("HLSL", "hlsl"),
    SAS("SAS", "sas"),
    RAKU("RAKU", "pl6"),
    RAZOR("RAZOR", "razor"),
    RESTRUCTUREDTEXT("RESTRUCTUREDTEXT", "rsp"),
    SEARCH_RESULT("SEARCH RESULT", "code-search"),
    SHADERLAB("SHADERLAB", "shader"),
    MARKDOWN_MATH("MARKDOWN MATH", "md"),
    MARKDOWN_MATH_BLOCK("MARKDOWN MATH BLOCK", "md"),
    MARKDOWN_MATH_INLINE("MARKDOWN MATH INLINE", "md"),
    MARKDOWN_LATEX_COMBINED("MARKDOWN LATEX COMBINED", "md"),
    MARKDOWN_MATH_CODE_BLOCK("MARKDOWN MATH CODE BLOCK", "md"),
    JIKESPG("JIKESPG", "jgrm"),
    SNIPPET("SNIPPET", "snippet"),
    SOURCE_SASSDOC("SOURCE SASSDOC", ""),
    SOURCE_C_PLATFORM("SOURCE C PLATFORM", ""),
    SOURCE_REGEXP_PYTHON("SOURCE REGEXP PYTHON", ""),
    DOCUMENTATION_INJECTION_TS("DOCUMENTATION INJECTION TS", ""),
    DOCUMENTATION_INJECTION_JSX("DOCUMENTATION INJECTION JS JSX", ""),
    TEXT_HTML_BASIC("TEXT HTML BASIC", ""),
    ;

    private final String name;

    private final String extension;

    public String getName() {
        return name;
    }

    public String getExtension() {
        return this.extension;
    }

    public String getSyntaxesName(){
        return this.toString().toLowerCase().replace("_","-");
    }

    EditorFormatType(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    /**
     * 从扩展名获取类型
     *
     * @param extName 扩展名
     * @return 类型
     */
    public static EditorFormatType ofExtension(String extName) {
        if (StringUtil.isNotBlank(extName)) {
            if (extName.equalsIgnoreCase("htm")) {
                return HTML;
            }
            for (EditorFormatType formatType : EditorFormatType.values()) {
                if (StringUtil.equalsIgnoreCase(formatType.getExtension(), extName)) {
                    return formatType;
                }
            }
            for (EditorFormatType formatType : EditorFormatType.values()) {
                if (StringUtil.equalsIgnoreCase(formatType.toString(), extName)) {
                    return formatType;
                }
            }
            for (EditorFormatType formatType : EditorFormatType.values()) {
                if (StringUtil.equalsIgnoreCase(formatType.getName(), extName)) {
                    return formatType;
                }
            }
        }
        return RAW;
    }

}
