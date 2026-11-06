package cn.oyzh.fx.editor.incubator;

import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.ThemeSettings;

import java.util.List;

/**
 * 编辑器样式提供者。
 * <p>
 * 不同 TextMate 主题对同一作用域的支持并不一致：例如部分主题只给链接加下划线、不设置颜色，
 * 个别主题则完全没有链接、文件路径、代码的样式规则。这会导致聊天语法中的网址、文件路径
 * 不易辨认（默认的 Primer Light 映射到 github-light-default 就是这种情况）。
 * <p>
 * 这里在主题解析结果之上，为以 {@code .llm} 结尾的聊天语法作用域补充一套稳定的配色：
 * 链接（蓝色+下划线）、文件路径（紫色）、命令（橙色）、代码（绿色）、危险命令（红色）等。
 * 该逻辑只匹配聊天语法自定义的作用域，不会影响其它格式。
 *
 * @author oyzh
 * @since 2026-11-06
 */
public class EditorStyleProvider extends StyleProvider {

    /**
     * 聊天语法作用域后缀
     */
    private static final String LLM_SCOPE_SUFFIX = ".llm";

    /**
     * 浅色主题配色
     */
    private static final Palette LIGHT = new Palette(
            Color.web("#0969DA"),
            Color.web("#8250DF"),
            Color.web("#953800"),
            Color.web("#116329"),
            Color.web("#CF222E"),
            Color.web("#0550AE"),
            Color.web("#9A6700"),
            Color.web("#6E7781")
    );

    /**
     * 深色主题配色
     */
    private static final Palette DARK = new Palette(
            Color.web("#58A6FF"),
            Color.web("#D2A8FF"),
            Color.web("#FFA657"),
            Color.web("#7EE787"),
            Color.web("#FF7B72"),
            Color.web("#79C0FF"),
            Color.web("#D29922"),
            Color.web("#8B949E")
    );

    /**
     * 当前主题配色
     */
    private @Nullable Palette palette;

    /**
     * 主题前景色，代码块普通文本使用
     */
    private @Nullable Color foreground;

    /**
     * 重置主题配色缓存
     */
    @Override
    public void flush() {
        super.flush();
        this.palette = null;
        this.foreground = null;
    }

    @Override
    protected @Nullable StyleAttributeMap resolveStyle(List<String> scopeStack) {
        StyleAttributeMap style = super.resolveStyle(scopeStack);
        String scope = lastScope(scopeStack);
        if (scope == null || !scope.endsWith(LLM_SCOPE_SUFFIX)) {
            return style;
        }
        Palette palette = this.palette();
        // 代码块内未被其它规则匹配的普通文本使用主题前景色，避免整块变成同一种颜色
        Color color;
        if (this.inFencedCode(scopeStack) && scope.contains("markup.inline.raw")) {
            color = this.foreground();
        } else {
            color = colorOf(palette, scope);
        }
        boolean underline = scope.contains("markup.underline.link") || scope.contains("link.title");
        boolean bold = scope.contains("markup.heading") || scope.contains("bold-italic");
        boolean italic = scope.contains("bold-italic");
        if (color == null && !underline && !bold && !italic) {
            return style;
        }
        StyleAttributeMap.Builder builder = StyleAttributeMap.builder();
        if (style != null) {
            builder.merge(style);
        }
        if (color != null) {
            builder.setTextColor(color);
        }
        // 链接统一下划线，避免主题只给了颜色时看不出是链接
        if (underline) {
            builder.setUnderline(true);
        }
        // 标题/粗斜体统一加粗、倾斜
        if (bold) {
            builder.setBold(true);
        }
        if (italic) {
            builder.setItalic(true);
        }
        // 危险命令统一去掉主题的斜体，保持警示样式一致
        if (scope.contains("invalid.illegal")) {
            builder.setItalic(false);
        }
        return builder.build();
    }

    /**
     * 根据作用域获取聊天语法的配色
     *
     * @param palette 当前配色
     * @param scope   作用域
     * @return 颜色，为空表示沿用主题配色
     */
    private static @Nullable Color colorOf(Palette palette, String scope) {
        // 日志级别
        if (scope.contains("log-level-error")) {
            return palette.danger();
        }
        if (scope.contains("log-level-warn")) {
            return palette.warning();
        }
        if (scope.contains("log-level-info")) {
            return palette.heading();
        }
        if (scope.contains("log-level-debug")) {
            return palette.muted();
        }
        if (scope.contains("log-thread") || scope.contains("log-timestamp")) {
            return palette.muted();
        }
        // JSON 键名
        if (scope.contains("property-name")) {
            return palette.command();
        }
        // 访问日志状态码
        if (scope.contains("log-status-error")) {
            return palette.danger();
        }
        if (scope.contains("log-status-success")) {
            return palette.code();
        }
        // 危险命令优先判断
        if (scope.contains("invalid.illegal")) {
            return palette.danger();
        }
        if (scope.contains("markup.underline.link")
                || scope.contains("link.title")
                || scope.contains("punctuation.definition.image")) {
            return palette.link();
        }
        if (scope.contains("file-path")) {
            return palette.path();
        }
        if (scope.contains("command")) {
            return palette.command();
        }
        if (scope.contains("markup.inline.raw") || scope.contains("markup.raw.block")) {
            return palette.code();
        }
        if (scope.contains("markup.heading") || scope.contains("definition.heading")) {
            return palette.heading();
        }
        if (scope.contains("keyword.other.task")) {
            return palette.command();
        }
        if (scope.contains("markup.hr")
                || scope.contains("punctuation.definition.list")
                || scope.contains("punctuation.definition.quote")
                || scope.contains("punctuation.definition.fenced")
                || scope.contains("punctuation.separator.table")
                || scope.contains("constant.character.escape")) {
            return palette.muted();
        }
        return null;
    }

    /**
     * 获取作用域栈中最后一个（最具体）的作用域
     *
     * @param scopeStack 作用域栈
     * @return 作用域
     */
    private static @Nullable String lastScope(List<String> scopeStack) {
        if (scopeStack == null || scopeStack.isEmpty()) {
            return null;
        }
        return scopeStack.get(scopeStack.size() - 1);
    }

    /**
     * 判断作用域是否位于代码块内
     *
     * @param scopeStack 作用域栈
     * @return 结果
     */
    private boolean inFencedCode(List<String> scopeStack) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            String scope = scopeStack.get(i);
            if (scope.contains("markup.fenced_code.block")) {
                return true;
            }
            if (scope.contains("markup.inline.raw.string")) {
                return false;
            }
        }
        return false;
    }

    /**
     * 获取主题前景色
     *
     * @return 前景色
     */
    private Color foreground() {
        if (this.foreground == null) {
            ThemeSettings settings = this.getThemeSettings();
            this.foreground = settings == null ? Color.BLACK : Color.web(settings.getForegroundColor());
        }
        return this.foreground;
    }

    /**
     * 获取当前主题配色（按主题背景明暗自动切换）
     *
     * @return 配色
     */
    private Palette palette() {
        if (this.palette != null) {
            return this.palette;
        }
        boolean dark = true;
        ThemeSettings settings = this.getThemeSettings();
        if (settings != null) {
            try {
                dark = Color.web(settings.getBackgroundColor()).getBrightness() < 0.5;
            } catch (Exception ignore) {
            }
        }
        this.palette = dark ? DARK : LIGHT;
        return this.palette;
    }

    /**
     * 聊天语法配色
     *
     * @param link    链接
     * @param path    文件路径
     * @param command 命令
     * @param code    代码
     * @param danger  危险命令
     * @param heading 标题
     * @param warning 警告日志
     * @param muted   弱化内容（列表符号、引用符号、分隔线等）
     */
    private record Palette(Color link, Color path, Color command, Color code, Color danger, Color heading,
                           Color warning, Color muted) {
    }
}
