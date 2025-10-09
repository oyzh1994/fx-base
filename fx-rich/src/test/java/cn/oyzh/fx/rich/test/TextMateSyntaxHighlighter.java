package cn.oyzh.fx.rich.test;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import tm4java.grammar.IGrammar;
import tm4java.grammar.IGrammarSource;
import tm4java.grammar.IToken;
import tm4java.grammar.ITokenizeLineResult;
import tm4java.registry.Registry;

import java.util.Collection;
import java.util.List;
// ... 其他必要的import

public class TextMateSyntaxHighlighter {

    private CodeArea codeArea;
    private IGrammar grammar;

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
        codeArea.getStylesheets().add("/style.css");
    }

    public void setupSyntaxHighlighting(String languageGrammarPath, String initialThemePath) {

        // a. 初始化TextMate注册表并加载语法
        Registry registry = new Registry();
        // 从类路径或文件系统加载TextMate语法文件(.tmLanguage.json)
        IGrammarSource grammarSource = IGrammarSource.fromResource(TextMateSyntaxHighlighter.class, languageGrammarPath);
        grammar = registry.addGrammar(grammarSource);


        // c. 设置样式工厂函数：这是核心，它告诉RichTextFX如何给文本加样式
        codeArea.richChanges() // 监听文本变化
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // 仅在真正的内容变化时触发
                .subscribe(change -> {
                    codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
                });
    }

    // d. 核心方法：计算指定文本的样式范围
    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        // 使用TextMate语法进行语法标记化（Tokenization）
        ITokenizeLineResult<IToken[]> result = grammar.tokenizeLine(text);

        int charIndex = 0;
        for (IToken token : result.tokens()) {
            // 将TextMate的scope（如"keyword.control.java"）转换为CSS样式类名
            String cssClass = convertScopesToCssClass(token.getScopes());
            System.out.println(cssClass);
            int len = token.getEndIndex();
            // 将样式应用到对应的文本范围
            spansBuilder.add(List.of(cssClass), len);
            charIndex += len;
        }

        return spansBuilder.create();
    }

    // e. 将TextMate的scope转换为CSS类名
    private String convertScopesToCssClass(List<String> scopes) {
        // 例如，将 "keyword.control.java" 转换为 "keyword-control"
        // 具体的转换逻辑和你的CSS定义相关
        // return scopes.get(scopes.size() - 1).replaceAll("\\.", "-").toLowerCase();

        String last = scopes.getLast();

        last=  last.substring(0, last.lastIndexOf("."));
        if(last.contains(".")){
        last=  last.substring(0, last.lastIndexOf("."));
        }

        // return last.toLowerCase();
        return last.replace(".", "-").toLowerCase();
    }
}