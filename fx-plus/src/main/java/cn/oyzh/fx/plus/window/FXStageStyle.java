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
    UTILITY,
    UNIFIED,
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
