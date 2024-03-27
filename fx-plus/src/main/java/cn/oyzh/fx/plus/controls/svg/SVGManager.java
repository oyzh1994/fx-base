package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.AnimationUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.animation.RotateTransition;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
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
            RotateTransition transition = glyph.getProp("_transition");
            if (transition == null) {
                transition = AnimationUtil.rotate(glyph);
                transition.setOnFinished(_ -> {
                    glyph.setRotate(0);
                    glyph.setWaiting(false);
                });
                SVGPathExt waiting = new SVGPathExt("/fx-plus/font/loading.svg");
                glyph.setProp("_shape", glyph.shape());
                glyph.setProp("_cursor", glyph.cursor());
                glyph.setProp("_color", glyph.getColor());
                glyph.setProp("_transition", transition);
                glyph.setShape(waiting);
                glyph.setCursor(Cursor.NONE);
                if (ThemeManager.isDarkMode()) {
                    glyph.setColor(Color.WHITE);
                } else {
                    glyph.setColor(Color.BLACK);
                }
            } else {
                FXUtil.runWait(transition::stop);
            }
            FXUtil.runLater(transition::play);
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
            FXUtil.runWait(() -> {
                glyph.setRotate(0);
                Color color = glyph.removeProp("_color");
                if (color != null) {
                    glyph.setColor(color);
                }
                Cursor cursor = glyph.removeProp("_cursor");
                if (cursor != null) {
                    glyph.cursor(cursor);
                }
                SVGPathExt shape = glyph.removeProp("_shape");
                if (shape != null) {
                    glyph.setShape(shape);
                }
                RotateTransition transition = glyph.removeProp("_transition");
                if (transition != null) {
                    transition.stop();
                }
            });
        }
    }
}
