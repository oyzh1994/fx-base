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
 * 校验工具类，提供校验失败时的界面提示处理
 *
 * @author oyzh
 * @since 2025-04-14
 */
public class ValidatorUtil {

    /**
     * 校验失败时进行提示处理，为组件添加红色边框并请求焦点
     *
     * @param target 组件
     */
    public static void validFail(EventTarget target) {
        validFail(target, 1500);
    }

    /**
     * 校验失败时进行提示处理，为组件添加红色边框并请求焦点，延迟指定时间后恢复原边框
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
