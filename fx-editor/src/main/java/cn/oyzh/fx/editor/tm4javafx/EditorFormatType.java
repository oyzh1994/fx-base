package cn.oyzh.fx.editor.tm4javafx;

import cn.oyzh.common.util.StringUtil;

/**
 * 富文本数据类型
 *
 * @author oyzh
 * @since 2024/5/17
 */
public enum EditorFormatType {
    // 原始格式
    RAW("RAW", ""),
    // 常见文件
    JSON("JSON", "json"),
    XML("XML", "xml"),
    HTML("HTML", "html,htm"),
    YAML("YAML", "yaml,yml"),
    CSS("CSS", "css"),
    PROPERTIES("PROPERTIES", "properties"),
    SHELLSCRIPT("SHELL", "sh"),
    BAT("BAT", "bat"),
    LOG("LOG", "log"),
    SQL("SQL", "sql"),
    DOCKERFILE("DOCKERFILE", "dockerfile"),
    MAKEFILE("MAKEFILE", "makefile"),
    MARKDOWN("MARKDOWN", "md"),
    CONF("CONFIG", "conf,cfg,config"),
    PROTO("Protocol Buffers", "proto"),
    JSONC("JSON with Comments", "jsonc"),
    JSONL("JSON Lines", "jsonl"),
    // 语言
    C("C", "c"),
    C_H("C header files", "h"),
    CPP("C++", "cpp"),
    CPP_H("C++ header files", "hpp,hxx"),
    JAVASCRIPT("JAVASCRIPT", "js"),
    JAVASCRIPTREACT("JAVASCRIPT (with React support)", "jsx"),
    PYTHON("PYTHON", "py"),
    JAVA("JAVA", "java"),
    CSHARP("C#", "cs"),
    CUDA_CPP("CUDA C++", "cu"),
    RUST("RUST", "rs"),
    RUBY("RUBY", "rb"),
    GO("GO", "go"),
    TYPESCRIPT("TYPESCRIPT", "ts"),
    TYPESCRIPTREACT("TYPESCRIPT (with React support)", "tsx"),
    PHP("PHP", "php"),
    LUA("LUA", "lua"),
    DART("DART", "dart"),
    PERL("PERL", "pl"),
    GROOVY("GROOVY", "groovy"),
    COFFEESCRIPT("COFFEESCRIPT", "coffee"),
    HANDLEBARS("HANDLEBARS", "hbs"),
    JSP("JSP", "jsp,jspf,tag"),
    KOTLIN("KOTLIN", "kt,kts", "xml"),
    R("R", "r"),
    SWIFT("SWIFT", "swift"),
    OBJECTIVE_C("OBJECTIVE C", "m"),
    OBJECTIVE_CPP("OBJECTIVE CPP", "mm"),
    FSHARP("FSHARP", "fs"),
    SCALA("SCALA", "scala,sbt"),
    JULIA("JULIA", "julia"),
    CLOJURE("CLOJURE", "clj"),
    ERLANG("ERLANG", "erl,escript,hrl,xrl,yrl", "xml"),
    ASM("ASM", "asm,s,inc"),
    CPP_EMBEDDED_LATEX("CPP EMBEDDED LATEX", "cpp"),
    // 配置文件
    CSV("CSV", "csv,tsv"),
    INI("INI", "ini"),
    VB("VB", "vb"),
    XSL("XSL", "xsl"),
    LESS("LESS", "less"),
    POWERSHELL("POWERSHELL", "ps1"),
    IGNORE("IGNORE", "ignore,gitignore"),
    LATEX("LATEX", "ltx"),
    SCSS("SCSS", "scss"),
    PUG("PUG", "pug"),
    TEX("TEX", "tex"),
    VIML("VIML", "vim,vimrc,gvimrc,nvimrc,_vimrc,vmb,ideavimrc"),
    GIT_COMMIT("GIT COMMIT", "txt"),
    GIT_REBASE("GIT REBASE", "git-rebase-todo"),
    BIBTEX("BIBTEX", "bib"),
    DIFF("DIFF", "diff"),
    KCONFIG("KCONFIG", "kconfig"),
    SVG("SVG", "svg"),
    // 无示例
    TERRAFORM("TERRAFORM", "tf,tfvars"),
    HCL("HCL", "hcl"),
    HLSL("HLSL", "hlsl"),
    SAS("SAS", "sas,sas7bdat"),
    // 无示例
    TWIG("TWIG", "twig,html.twig", "xml"),
    RAKU("RAKU", "pl6"),
    RAZOR("RAZOR", "razor"),
    RESTRUCTUREDTEXT("RESTRUCTUREDTEXT", "rst"),
    SEARCH_RESULT("SEARCH RESULT", "code-search"),
    SHADERLAB("SHADERLAB", "shader"),
    MARKDOWN_MATH("MARKDOWN MATH", "md"),
    MARKDOWN_MATH_BLOCK("MARKDOWN MATH BLOCK", "md"),
    MARKDOWN_MATH_INLINE("MARKDOWN MATH INLINE", "md"),
    MARKDOWN_LATEX_COMBINED("MARKDOWN LATEX COMBINED", "md"),
    MARKDOWN_MATH_CODE_BLOCK("MARKDOWN MATH CODE BLOCK", "md"),
    // 无示例
    JIKESPG("JIKESPG", "g"),
    // 无示例
    SNIPPETS("SNIPPETS", "snippet,snippets"),
    SOURCE_SASSDOC("SOURCE SASSDOC", ""),
    SOURCE_C_PLATFORM("SOURCE C PLATFORM", ""),
    SOURCE_REGEXP_PYTHON("SOURCE REGEXP PYTHON", ""),
    DOCUMENTATION_INJECTION_TS("DOCUMENTATION INJECTION TS", ""),
    DOCUMENTATION_INJECTION_JSX("DOCUMENTATION INJECTION JS JSX", ""),
    TEXT_HTML_BASIC("TEXT HTML BASIC", ""),
    MDX("MDX", ""),
    MDX_MARKDOWN("MDX MARKDOWN", ""),
    ASCIIDOCTOR("ASCIIDOCTOR", "ad,asc,adoc,asciidoc"),
    // 无示例
    BICEP("BICEP", "bicep", "xml"),
    NSIS_SCRIPT("NSIS SCRIPT", "nsh,nsi"),
    XAML("XAML", "xaml"),
    FXML("FXML", "fxml"),
    HOSTS("HOSTS", "hosts,host"),
    PROFILE("PROFILE", "bashrc,profile,bash_profile,zshrc,env"),
    SSHD_CONFIG("SSHD CONFIG", "sshd_config"),
    ;

    private final String name;

    private final String extension;

    private String textMateType = "json";

    public String getName() {
        return name;
    }

    public String getExtension() {
        return this.extension;
    }

    public String getFirstExtension() {
        if (this.extension.contains(",")) {
            return this.extension.split(",")[0];
        }
        return this.extension;
    }

    public String getTextMateType() {
        return this.extension;
    }

    /**
     * 获取语法名称
     *
     * @return 语法名称
     */
    public String getSyntaxesName() {
        // c头文件类型重定位
        if (this == C_H) {
            return C.getSyntaxesName();
        }
        // c++头文件类型重定位
        if (this == CPP_H) {
            return CPP.getSyntaxesName();
        }
        return this.toString().toLowerCase().replace("_", "-");
    }

    /**
     * 获取语法全称
     *
     * @return 语法全称
     */
    public String getFullSyntaxesName() {
        String name = this.getSyntaxesName();
        name += ".tmLanguage";
        if (StringUtil.isNotBlank(this.textMateType)) {
            name += "." + this.textMateType;
        }
        return name;
    }

    EditorFormatType(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    EditorFormatType(String name, String extension, String textMateType) {
        this.name = name;
        this.extension = extension;
        this.textMateType = textMateType;
    }

    /**
     * 从扩展名获取类型
     *
     * @param extName 扩展名
     * @return 类型
     */
    public static EditorFormatType ofExtension(String extName) {
        if (StringUtil.isNotBlank(extName)) {
            for (EditorFormatType formatType : EditorFormatType.values()) {
                String extension = formatType.getExtension();
                if (extension.isEmpty()) {
                    continue;
                }
                if (extension.contains(",")) {
                    if (StringUtil.equalsAnyIgnoreCase(extName, extension.split(","))) {
                        return formatType;
                    }
                } else if (StringUtil.equals(extName, extension)) {
                    return formatType;
                }
            }
        }
        return RAW;
    }

}
