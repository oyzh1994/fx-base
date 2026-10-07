package cn.oyzh.fx.plus.window;

import javafx.stage.StageStyle;

/**
 * 窗口风格，对应 JavaFX 的 {@link StageStyle}
 *
 * @author oyzh
 * @since 2024-12-16
 */
public enum FXStageStyle {

    /**
     * 装饰窗口，带有系统标题栏与边框
     */
    DECORATED,
    /**
     * 无装饰窗口，不显示系统标题栏与边框
     */
    UNDECORATED,
    /**
     * 透明窗口，背景透明且无装饰
     */
    TRANSPARENT,
    /**
     * 这个会导致部分windows环境下页面白屏，不要使用
     */
    @Deprecated
    UTILITY,
    /**
     * 这个会导致部分windows环境下页面白屏，不要使用
     */
    @Deprecated
    UNIFIED,
    /**
     * 扩展窗口，保留系统装饰但将内容区扩展到标题栏
     */
    EXTENDED,
    /**
     * 自定义窗口
     */
    @Deprecated
    CUSTOM;

    /**
     * 转换为 JavaFX 舞台风格
     *
     * @return 舞台风格
     */
    public StageStyle toStageStyle() {
        return switch (this) {
            case DECORATED -> StageStyle.DECORATED;
            case UNDECORATED, CUSTOM -> StageStyle.UNDECORATED;
            case TRANSPARENT -> StageStyle.TRANSPARENT;
            case UTILITY -> StageStyle.UTILITY;
            case UNIFIED -> StageStyle.UNIFIED;
            case EXTENDED -> StageStyle.EXTENDED;
        };
    }

    /**
     * 是否扩展类型
     *
     * @return 结果
     */
    public boolean isExtended() {
        return this == EXTENDED;
    }

    /**
     * 是否自定义类型
     *
     * @return 结果
     */
    public boolean isCustom() {
        return this == CUSTOM;
    }
}
