package cn.oyzh.fx.gui.skin;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.skin.FXTextFieldSkin;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * 文本域皮肤扩展
 *
 * @author oyzh
 * @since 2023/10/25
 */
public class ActionTextFieldSkin extends FXTextFieldSkin {

    private Runnable action;

    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    protected final SVGGlyph button;

    public ActionTextFieldSkin(TextField control, SVGGlyph button) {
        super(control);
        this.button = button;
        this.button.managedBindVisible();
        this.button.setEnableWaiting(false);
//        this.button.setFocusTraversable(false);
//        this.button.setPadding(new Insets(0));
        this.button.setColor(this.getButtonColor());
        this.button.setOnMousePrimaryClicked(this::onButtonClicked);
        this.button.setOnMouseExited(mouseEvent -> this.resetButtonColor());
        this.button.setOnMouseMoved(mouseEvent -> this.button.setColor("#DC143C"));
        this.getChildren().add(this.button);
    }

    protected void onButtonClicked(MouseEvent e) {
        if (this.action != null) {
            this.action.run();
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // 按钮大小，组件高度/2，最大20，最小10
        double size = NumberUtil.limit(h * 0.5, this.getButtonSizeMin(), this.getButtonSizeMax());
        // 设置按钮大小
        this.setButtonSize(size);
        // 计算组件大小
        double btnSize = this.snapSizeX(size);
        // 位移的areaX值，规则 组件宽 + x -按钮大小
        double areaX = w + x - btnSize - 5;
        // 设置位置
        // super.positionInArea(this.button, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
        super.positionInArea(this.button, areaX, y, 0, h, 0, HPos.CENTER, VPos.CENTER);
    }

    /**
     * 设置按钮大小
     *
     * @param size 大小
     */
    protected void setButtonSize(double size) {
        this.button.setSize(size);
    }

//    @Override
//    protected Color getButtonColor() {
//        if (!ThemeManager.isDarkMode()) {
//            return Color.valueOf("#696969");
//        }
//        return super.getButtonColor();
//    }

    public void resetButtonColor() {
        this.button.setColor(this.getButtonColor());
    }

    protected double getButtonSizeMin() {
        return 10;
    }

    protected double getButtonSizeMax() {
        return 20;
    }
}
