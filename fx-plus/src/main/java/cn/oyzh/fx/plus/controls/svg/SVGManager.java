package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.util.AnimationUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.animation.RotateTransition;
import javafx.scene.Cursor;
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
            FXUtil.runLater(() -> {
                RotateTransition transition = glyph.getProp("_transition");
                if (transition == null) {
                    transition = AnimationUtil.rotate(glyph);
                    transition.setOnFinished(actionEvent -> {
                        glyph.setRotate(0);
                        glyph.setWaiting(false);
                    });
                    SVGPathExt waiting = new SVGPathExt("/fx-plus/font/loading.svg");
                    glyph.setProp("_shape", glyph.shape());
                    glyph.setProp("_transition", transition);
                    glyph.setProp("_cursor", glyph.cursor());
                    glyph.setShape(waiting);
                    glyph.setCursor(Cursor.NONE);
                } else {
                    transition.stop();
                }
                transition.play();
            });
        }
    }

    /**
     * 停止等待中动画
     *
     * @param glyph svg图标
     */
    public static void stopWaiting(SVGGlyph glyph) {
        if (glyph != null) {
            FXUtil.runLater(() -> {
                RotateTransition transition = glyph.removeProp("_transition");
                if (transition != null) {
                    transition.stop();
                }
                Cursor cursor = glyph.removeProp("_cursor");
                SVGPathExt shape = glyph.removeProp("_shape");
                if (shape != null) {
                    glyph.setShape(shape);
                }
                if (cursor != null) {
                    glyph.cursor(cursor);
                }
                glyph.setRotate(0);
                glyph.setWaiting(false);
            });
        }
    }
}
