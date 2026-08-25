package cn.oyzh.fx.editor.incubator;

/**
 * 编辑器着色异步策略
 *
 * @author oyzh
 * @since 2026/5/27
 */
public enum EditorSyntaxAsyncStrategy {

    /**
     * 首次
     */
    FIRST,

    /**
     * 永远
     */
    ALWAYS,

    /**
     * 自动，非首次渲染情况下，小于自动阈值则会回退到同步，否则异步
     */
    AUTO;
}
