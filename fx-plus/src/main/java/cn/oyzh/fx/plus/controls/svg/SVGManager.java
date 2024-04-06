package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.util.FXUtil;
import lombok.experimental.UtilityClass;

/**
 * svg管理器
 *
 * @author oyzh
 * @since 2023/9/15
 */
@UtilityClass
public class SVGManager {

    /**
     * 执行等待中动画
     *
     * @param glyph svg图标
     */
    public static void startWaiting(SVGGlyph glyph) {
        if (glyph != null) {
            glyph.setWaiting(true);
            FXUtil.runWait(glyph::initWaiting);
        }
    }

    /**
     * 停止等待中动画
     *
     * @param glyph svg图标
     */
    public static void stopWaiting(SVGGlyph glyph) {
        if (glyph != null) {
            glyph.setWaiting(false);
            FXUtil.runLater(glyph::initContent);
        }
    }
}
