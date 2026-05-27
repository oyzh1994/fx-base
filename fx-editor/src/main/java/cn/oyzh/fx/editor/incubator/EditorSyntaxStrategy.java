package cn.oyzh.fx.editor.incubator;

/**
 * 编辑器着色策略
 *
 * @author oyzh
 * @since 2026/5/27
 */
public enum EditorSyntaxStrategy {

    /**
     * 同步
     */
    SYNC,

    /**
     * 异步
     */
    ASYNC,

    /**
     * 自动，小于最小阈值则同步，否则异步
     */
    AUTO;
}
