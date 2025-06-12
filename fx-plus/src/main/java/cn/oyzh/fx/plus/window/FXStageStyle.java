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
    @Deprecated
    CUSTOM;

    public StageStyle toStageStyle() {
        return switch (this) {
            case DECORATED -> StageStyle.DECORATED;
            case UNDECORATED, CUSTOM -> StageStyle.UNDECORATED;
            case TRANSPARENT -> StageStyle.TRANSPARENT;
            case UTILITY -> StageStyle.UTILITY;
            case UNIFIED -> StageStyle.UNIFIED;
        };
    }

    public boolean isCustom() {
        return this == CUSTOM;
    }
}
