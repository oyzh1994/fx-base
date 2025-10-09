package cn.oyzh.fx.plus.mouse;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * 鼠标工具类
 *
 * @author oyzh
 * @since 2023/2/28
 */
public class MouseUtil {

    public static double getMouseX() {
        return FXUtil.getRobot().getMouseX();
    }

    public static double getMouseY() {
        return FXUtil.getRobot().getMouseY();
    }

    public static double[] getMousePosition() {
        double[] position = new double[2];
        FXUtil.runWait(() -> {
            position[0] = FXUtil.getRobot().getMouseX();
            position[1] = FXUtil.getRobot().getMouseY();
        });
        return position;
    }

    public static Point2D getMousePoint() {
        return new Point2D(getMouseX(), getMouseY());
    }

    /**
     * 是否单击
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isSingleClick(MouseEvent event) {
        return event != null && event.getClickCount() == 1;
    }

    /**
     * 是否双击
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isDubboClick(MouseEvent event) {
        return event != null && event.getClickCount() > 1;
    }

    /**
     * 是否鼠标主按钮
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isPrimaryButton(MouseEvent event) {
        return event != null && event.getButton() == MouseButton.PRIMARY;
    }

    /**
     * 是否鼠标中间按钮
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isMiddleButton(MouseEvent event) {
        return event != null && event.getButton() == MouseButton.MIDDLE;
    }

    /**
     * 是否鼠标次按钮
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isSecondButton(MouseEvent event) {
        return event != null && event.getButton() == MouseButton.SECONDARY;
    }

    /**
     * 是否鼠标回退按钮
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isBackButton(MouseEvent event) {
        return event != null && event.getButton() == MouseButton.BACK;
    }

    /**
     * 是否鼠标前进按钮
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isForwardButton(MouseEvent event) {
        return event != null && event.getButton() == MouseButton.FORWARD;
    }
}
