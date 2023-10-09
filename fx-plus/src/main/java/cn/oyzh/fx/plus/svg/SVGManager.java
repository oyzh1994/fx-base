package cn.oyzh.fx.plus.svg;

import cn.oyzh.fx.plus.util.AnimationUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.animation.RotateTransition;
import javafx.scene.Cursor;
import javafx.scene.shape.SVGPath;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2023/9/15
 */
@UtilityClass
public class SVGManager {

    public static void startWaiting(SVGGlyph glyph) {
        FXUtil.runLater(() -> {
            RotateTransition transition = glyph.getProp("_svgTransition");
            if (transition == null) {
                transition = AnimationUtil.rotate(glyph);
                SVGPath waiting = new SVGPath();
                waiting.setContent(SVGLoader.INSTANCE.load("/font/loading.svg"));
                glyph.setProp("_svgTransition", transition);
                glyph.setProp("_svgCursor", glyph.getSvgCursor());
                glyph.setProp("_shape", glyph.svgPath(false));
                glyph.setShape(waiting);
                glyph.setCursor(Cursor.NONE);
                glyph.setWaiting(true);
            } else {
                transition.stop();
            }
            transition.play();
        });
    }

    public static void stopWaiting(SVGGlyph glyph) {
        FXUtil.runLater(() -> {
            RotateTransition transition = glyph.removeProp("_svgTransition");
            if (transition != null) {
                transition.stop();
            }
            SVGPath shape = glyph.removeProp("_shape");
            Cursor svgCursor = glyph.removeProp("_svgCursor");
            if (shape != null) {
                glyph.setShape(shape);
            }
            if (svgCursor != null) {
                glyph.setSvgCursor(svgCursor);
            }
            glyph.setRotate(0);
            glyph.setWaiting(false);
        });
    }
}
