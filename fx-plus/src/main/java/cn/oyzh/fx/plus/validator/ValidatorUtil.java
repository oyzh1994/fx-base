package cn.oyzh.fx.plus.validator;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.EventTarget;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2025-04-14
 */
public class ValidatorUtil {

    /**
     * 组校验失败，提示处理
     *
     * @param target 组件
     */
    public static void validFail(EventTarget target) {
        validFail(target, 1500);
    }

    /**
     * 组校验失败，提示处理
     *
     * @param target 组件
     * @param delay  延迟恢复时间，-1不再恢复
     */
    public static void validFail(EventTarget target, int delay) {
        if (target instanceof Region region) {
            Border original = region.getBorder();
            CornerRadii radii;
            if (original != null && original.getStrokes() != null && !original.getStrokes().isEmpty()) {
                radii = original.getStrokes().getFirst().getRadii();
            } else {
                radii = CornerRadii.EMPTY;
            }
            Border border = new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, radii, BorderWidths.DEFAULT));
            region.setBorder(border);
            region.requestFocus();
            if (delay > 0) {
                FXUtil.runLater(() -> region.setBorder(original), delay);
            }
        }
    }
}
