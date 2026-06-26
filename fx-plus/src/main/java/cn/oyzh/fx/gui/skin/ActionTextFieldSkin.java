package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.skin.FXTextFieldSkin;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * 文本域皮肤扩展
 *
 * @author oyzh
 * @since 2023/10/25
 */
public abstract class ActionTextFieldSkin extends FXTextFieldSkin {

    /**
     * 操作
     */
    private Runnable action;

    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    /**
     * 当前按钮
     */
    protected SVGGlyph button;

    public ActionTextFieldSkin(TextField control) {
        super(control);
        // this.button = button;
        // this.button.managedBindVisible();
        // this.button.setEnableWaiting(false);
        // this.button.setFocusTraversable(false);
        // this.button.setPadding(new Insets(0));
        // this.button.setColor(this.getButtonColor());
        // this.button.setOnMousePrimaryClicked(this::onButtonClicked);
        // this.button.setOnMouseExited(mouseEvent -> this.resetButtonColor());
        // this.button.setOnMouseMoved(mouseEvent -> this.button.setColor("#DC143C"));
        // this.getChildren().add(this.button);
    }

    /**
     * 初始化按钮
     *
     * @param button 按钮
     */
    protected void initButton(SVGGlyph button) {
        button.setPadding(Insets.EMPTY);
        button.setFocusTraversable(false);
        button.addEventFilter(MouseEvent.MOUSE_EXITED, this::onButtonExit);
        button.addEventFilter(MouseEvent.MOUSE_ENTERED, this::onButtonEnter);
        button.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onButtonClick);
    }

    /**
     * 获取按钮
     *
     * @return 结果
     */
    protected abstract SVGGlyph getButton();

    /**
     * 按钮鼠标点击事件
     *
     * @param e 事件
     */
    protected void onButtonClick(MouseEvent e) {
        if (this.action != null) {
            this.action.run();
        }
    }

    /**
     * 按钮鼠标离开事件
     *
     * @param e 事件
     */
    protected void onButtonExit(MouseEvent e) {
        this.resetButtonColor();
    }

    /**
     * 按钮鼠标进入事件
     *
     * @param e 事件
     */
    protected void onButtonEnter(MouseEvent e) {
        this.button.setColor("#DC143C");
    }

    // @Override
    // protected void layoutChildren(double x, double y, double w, double h) {
    //     super.layoutChildren(x, y, w, h);
    //     // 按钮大小，组件高度/2，最大20，最小10
    //     double size = NumberUtil.limit(h * 0.5, this.getButtonSizeMin(), this.getButtonSizeMax());
    //     // 设置按钮大小
    //     this.setButtonSize(size);
    //     // 计算组件大小
    //     double btnSize = this.snapSizeX(size);
    //     // 位移的areaX值，规则 组件宽 + x -按钮大小
    //     double areaX = w + x - btnSize - this.getButtonMargin();
    //     // 设置位置
    //     // super.positionInArea(this.button, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
    //     super.positionInArea(this.button, areaX, y, 0, h, 0, HPos.CENTER, VPos.CENTER);
    // }

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

    /**
     * 重置按钮颜色
     */
    public void resetButtonColor() {
        this.button.setColor(this.getButtonColor());
    }

    /**
     * 获取按钮大小最小值
     *
     * @return 结果
     */
    protected double getButtonSizeMin() {
        return 8;
    }

    /**
     * 获取按钮大小最大值
     *
     * @return 结果
     */
    protected double getButtonSizeMax() {
        return 14;
    }

    @Override
    public ObjectProperty<Node> rightProperty() {
        if (super.rightProperty == null) {
            super.rightProperty().set(this.getButton());
        }
        return super.rightProperty();
    }

    @Override
    public void dispose() {
        this.button.removeEventFilter(MouseEvent.MOUSE_EXITED, this::onButtonExit);
        this.button.removeEventFilter(MouseEvent.MOUSE_ENTERED, this::onButtonEnter);
        this.button.removeEventFilter(MouseEvent.MOUSE_CLICKED, this::onButtonClick);
        this.button = null;
        this.action = null;
        super.dispose();
    }

    @Override
    protected void onSizeChanged() {
        super.onSizeChanged();
        TextField textField = this.getSkinnable();
        double h = NodeUtil.getHeight(textField);
        double size = h * 0.8;
        if (size < this.getButtonSizeMin()) {
            size = this.getButtonSizeMin();
        } else if (size > this.getButtonSizeMax()) {
            size = this.getButtonSizeMax();
        }
        // 设置大小
        this.setButtonSize(size);
    }

    /**
     * 默认左侧组件内边距
     */
    public static final Insets DEFAULT_LEFT_PADDING = new Insets(0, 0, 0, 5);

}
