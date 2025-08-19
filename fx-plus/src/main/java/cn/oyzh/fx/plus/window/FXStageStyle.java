package cn.oyzh.fx.plus.window;

import javafx.stage.StageStyle;

/**
 * @author oyzh
 * @since 2024-12-16
 */
public enum FXStageStyle {

    DECORATED,
    UNDECORATED,
    TRANSPARENT,
    /**
     * 这个会导致部分windows环境下页面白屏，不要使用
     */
    @Deprecated
    UTILITY,
    UNIFIED,
    EXTENDED,
    @Deprecated
    CUSTOM;

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
