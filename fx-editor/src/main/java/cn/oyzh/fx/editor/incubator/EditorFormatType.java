package cn.oyzh.fx.editor.incubator;

import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑器格式类型
 *
 * @author oyzh
 * @since 2024-05-17
 */
public enum EditorFormatType {
    // 常用文本与数据
    RAW("RAW", ""),
    JSON("JSON", "json"),
    PROTO("Protocol Buffers", "proto"),
    YAML("YAML", "yaml,yml"),
    XML("XML", "xml,admx,adml,artdef,resx,pom,svg,ps1xml,filters,kuip"),
    HTML("HTML", "html,htm,ftl"),
    MARKDOWN("MARKDOWN", "md"),
    CSS("CSS", "css"),
    SQL("SQL", "sql"),
    PROPERTIES("PROPERTIES", "properties"),
    SHELLSCRIPT("SHELL", "sh"),
    BAT("BAT", "bat"),
    INI("INI", "ini,repositories"),
    TOML("TOML", "toml"),
    CSV("CSV", "csv,tsv"),
    JSONC("JSON with Comments", "jsonc"),
    JSONL("JSON Lines", "jsonl"),
    DIFF("DIFF", "diff"),
    LOG("LOG", "log"),
    CONF("CONFIG", "conf,cfg,config,fsc"),
    DOTENV("DOTENV", "env,dotenv"),
    EDITORCONFIG("EDITORCONFIG", "editorconfig"),
    HTTP("HTTP", "http,rest"),
    GRAPHQL("GRAPHQL", "graphql,gql"),
    REGEXP("REGEXP", "regexp,regex"),
    HOSTS("HOSTS", "hosts,host"),
    PROFILE("PROFILE", "bashrc,profile,bash_profile,zshrc"),
    SSHD_CONFIG("SSHD CONFIG", "sshd_config"),
    // 编程语言（按常见度）
    JAVASCRIPT("JAVASCRIPT", "js"),
    TYPESCRIPT("TYPESCRIPT", "ts"),
    JAVASCRIPTREACT("JAVASCRIPT (with React support)", "jsx"),
    TYPESCRIPTREACT("TYPESCRIPT (with React support)", "tsx"),
    JAVA("JAVA", "java"),
    PYTHON("PYTHON", "py"),
    C("C", "c,h,frag"),
    CPP("C++", "cpp,hpp,hxx"),
    CSHARP("C#", "cs"),
    GO("GO", "go"),
    RUST("RUST", "rs"),
    PHP("PHP", "php"),
    RUBY("RUBY", "rb"),
    KOTLIN("KOTLIN", "kt,kts", "xml"),
    SWIFT("SWIFT", "swift"),
    SCALA("SCALA", "scala,sbt"),
    GROOVY("GROOVY", "groovy"),
    DART("DART", "dart"),
    LUA("LUA", "lua"),
    PERL("PERL", "pl"),
    R("R", "r"),
    JULIA("JULIA", "julia"),
    OBJECTIVE_C("OBJECTIVE C", "m"),
    OBJECTIVE_CPP("OBJECTIVE CPP", "mm"),
    FSHARP("FSHARP", "fs"),
    VB("VB", "vb"),
    CUDA_CPP("CUDA C++", "cu,cuh"),
    ASM("ASM", "asm,s,inc"),
    HASKELL("HASKELL", "hs,lhs"),
    ELIXIR("ELIXIR", "ex,exs"),
    ERLANG("ERLANG", "erl,escript,hrl,xrl,yrl", "xml"),
    OCAML("OCAML", "ml,mli"),
    CLOJURE("CLOJURE", "clj"),
    SOLIDITY("SOLIDITY", "sol"),
    ZIG("ZIG", "zig"),
    NIX("NIX", "nix"),
    TCL("TCL", "tcl"),
    FORTRAN("FORTRAN", "f,f77,f90,f95,f03,f08,for"),
    PASCAL("PASCAL", "pas,pp,dpr"),
    COFFEESCRIPT("COFFEESCRIPT", "coffee"),
    RAKU("RAKU", "pl6"),
    SAS("SAS", "sas,sas7bdat"),
    HLSL("HLSL", "hlsl"),
    SHADERLAB("SHADERLAB", "shader"),
    JIKESPG("JIKESPG", "g"),
    WAT("WEBASSEMBLY TEXT", "wat,wast"),
    AWK("AWK", "awk,gawk,mawk"),
    ACCESS("ACCESS", "access"),
    VIML("VIML", "vim,vimrc,gvimrc,nvimrc,_vimrc,vmb,ideavimrc"),
    POWERSHELL("POWERSHELL", "ps1"),
    // Web、模板与标记
    SCSS("SCSS", "scss"),
    LESS("LESS", "less"),
    PUG("PUG", "pug"),
    HANDLEBARS("HANDLEBARS", "hbs"),
    TWIG("TWIG", "twig", "xml"),
    RAZOR("RAZOR", "razor"),
    JSP("JSP", "jsp,tag,jspf"),
    ASCX("ASCX", "ascx"),
    XSL("XSL", "xsl"),
    XAML("XAML", "xaml"),
    FXML("FXML", "fxml"),
    MDX("MDX", ""),
    MDX_MARKDOWN("MDX MARKDOWN", ""),
    MARKDOWN_MATH("MARKDOWN MATH", "md"),
    MARKDOWN_MATH_BLOCK("MARKDOWN MATH BLOCK", "md"),
    MARKDOWN_MATH_INLINE("MARKDOWN MATH INLINE", "md"),
    MARKDOWN_MATH_CODE_BLOCK("MARKDOWN MATH CODE BLOCK", "md"),
    MARKDOWN_LATEX_COMBINED("MARKDOWN LATEX COMBINED", "md"),
    TEXT_HTML_BASIC("TEXT HTML BASIC", ""),
    // 文档与排版
    LATEX("LATEX", "ltx"),
    TEX("TEX", "tex"),
    BIBTEX("BIBTEX", "bib"),
    RESTRUCTUREDTEXT("RESTRUCTUREDTEXT", "rst"),
    ASCIIDOCTOR("ASCIIDOCTOR", "adoc,asciidoc,ad,asc"),
    // 构建、配置与运维
    DOCKERFILE("DOCKERFILE", "dockerfile"),
    MAKEFILE("MAKEFILE", "makefile"),
    CMAKE("CMAKE", "cmake"),
    GRADLE("GRADLE", "gradle"),
    BICEP("BICEP", "bicep", "xml"),
    STARLARK("STARLARK", "bzl,bazel,starlark,sky,star"),
    TERRAFORM("TERRAFORM", "tf,tfvars"),
    HCL("HCL", "hcl"),
    KCONFIG("KCONFIG", "kconfig"),
    INF("INF", "inf,ks"),
    NGINX("NGINX", "nginx"),
    NSIS_SCRIPT("NSIS SCRIPT", "nsh,nsi"),
    GIT_COMMIT("GIT COMMIT", ""),
    GIT_REBASE("GIT REBASE", "git-rebase-todo"),
    IGNORE("IGNORE", "ignore,gitignore"),
    SNIPPETS("SNIPPETS", "snippet,snippets"),
    SEARCH_RESULT("SEARCH RESULT", "code-search"),
    // 内部语法与注入
    SOURCE_SASSDOC("SOURCE SASSDOC", ""),
    SOURCE_C_PLATFORM("SOURCE C PLATFORM", ""),
    SOURCE_REGEXP_PYTHON("SOURCE REGEXP PYTHON", ""),
    DOCUMENTATION_INJECTION_TS("DOCUMENTATION INJECTION TS", ""),
    DOCUMENTATION_INJECTION_JS("DOCUMENTATION INJECTION JS", ""),
    CPP_EMBEDDED_LATEX("CPP EMBEDDED LATEX", "cpp"),
    ;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 文件扩展名，多个以英文逗号分隔
     */
    private final String extension;

    /**
     * textmate类型
     */
    private String textMateType = "json";

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取文件扩展名
     *
     * @return 文件扩展名
     */
    public String getExtension() {
        return this.extension;
    }

    /**
     * 获取首个文件扩展名
     *
     * @return 首个文件扩展名
     */
    public String getFirstExtension() {
        if (this.extension.contains(",")) {
            return this.extension.split(",")[0];
        }
        return this.extension;
    }

    /**
     * 获取textmate类型
     *
     * @return textmate类型
     */
    public String getTextMateType() {
        return this.textMateType;
    }

    /**
     * 获取语法名称
     *
     * @return 语法名称
     */
    public String getSyntaxesName() {
        //// c头文件类型重定位
        //if (this == C_H) {
        //    return C.getSyntaxesName();
        //}
        //// c++头文件类型重定位
        //if (this == CPP_H) {
        //    return CPP.getSyntaxesName();
        //}
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

    /**
     * 构造编辑器格式类型对象。
     *
     * @param name 名称
     * @param extension 扩展名
     */
    EditorFormatType(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    /**
     * 构造编辑器格式类型对象。
     *
     * @param name 名称
     * @param extension 扩展名
     * @param textMateType 文本Mate类型
     */
    EditorFormatType(String name, String extension, String textMateType) {
        this.name = name;
        this.extension = extension;
        this.textMateType = textMateType;
    }

    /**
     * 按关键字过滤格式，支持名称、枚举名和扩展名。
     *
     * @param keyword 关键字
     * @return 过滤结果
     */
    public static List<EditorFormatType> filter(String keyword) {
        String text = keyword == null ? "" : keyword.trim();
        List<EditorFormatType> formats = new ArrayList<>();
        for (EditorFormatType formatType : values()) {
            if (text.isEmpty()
                    || StringUtil.containsIgnoreCase(formatType.getName(), text)
                    || StringUtil.containsIgnoreCase(formatType.name(), text)
                    || StringUtil.containsIgnoreCase(formatType.getExtension(), text)) {
                formats.add(formatType);
            }
        }
        return formats;
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
                } else if (StringUtil.equalsIgnoreCase(extName, extension)) {
                    return formatType;
                }
            }
        }
        return RAW;
    }

}
