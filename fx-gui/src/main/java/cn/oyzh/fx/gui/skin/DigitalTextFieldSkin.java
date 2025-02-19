package cn.oyzh.fx.gui.skin;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.skin.FXTextFieldSkin;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2023/10/9
 */
public class DigitalTextFieldSkin extends FXTextFieldSkin {

    /**
     * 增加按钮
     */
    protected SVGGlyph incrButton;

    /**
     * 减少按钮
     */
    protected SVGGlyph decrButton;

    /**
     * 按钮右边距
     */
    @Getter
    @Setter
    private float btnMarginRight = 5.0f;

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean isEmpty = this.getSkinnable().getText() == null || this.getSkinnable().getText().isEmpty();
        boolean shouldBeVisible = !disable && visible && hasFocus && !isEmpty;
        this.decrButton.setVisible(shouldBeVisible);
        this.incrButton.setVisible(shouldBeVisible);
    }

    public DigitalTextFieldSkin(TextField textField, Runnable onIncr, Runnable onDecr) {
        super(textField);
        double h = textField.getHeight() / 2.d - 1;
        if (h <= 0) {
            h = 10;
        }
        // 初始化增加、减少按钮
        this.incrButton = new SVGGlyph("/fx-svg/arrow-up-filling.svg");
        this.incrButton.setSize(h);
        this.incrButton.setVisible(false);
        this.incrButton.setTipText(I18nHelper.addValue());
        this.incrButton.managedBindVisible();
        this.incrButton.setEnableWaiting(false);
        this.incrButton.setFocusTraversable(false);

        this.decrButton = new SVGGlyph("/fx-svg/arrow-down-filling.svg");
        this.decrButton.setSize(h);
        this.decrButton.setVisible(false);
        this.decrButton.setColor(this.getButtonColor());
        this.decrButton.setTipText(I18nHelper.reduceValue());
        this.decrButton.managedBindVisible();
        this.decrButton.setEnableWaiting(false);
        this.decrButton.setFocusTraversable(false);
        // 绑定事件到按钮
        if (onIncr != null) {
            this.incrButton.setOnMousePrimaryClicked(event -> onIncr.run());
        }
        if (onDecr != null) {
            this.decrButton.setOnMousePrimaryClicked(event -> onDecr.run());
        }
        // 执行事件过滤
        if (onIncr != null || onDecr != null) {
            textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.UP) {
                    if (onIncr != null) {
                        onIncr.run();
                    }
                    event.consume();
                } else if (event.getCode() == KeyCode.DOWN) {
                    if (onDecr != null) {
                        onDecr.run();
                    }
                    event.consume();
                }
            });
        }
        // 添加到组件
        this.getChildren().addAll(this.incrButton, this.decrButton);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // 文本域高度
        double height = NodeUtil.getHeight(this.getSkinnable());
        // 按钮大小，规则 组件高*0.5-3
        double size = Math.floor(height * 0.5) - 3;
        // 限制按钮大小
        size = NumberUtil.limit(size, 5, 13);
        this.incrButton.setSize(size);
        this.decrButton.setSize(size);
        // 计算按钮实际大小
        double btnSize = this.snapSizeX(size);
        // 位移的areaX值，规则 组件宽+x-按钮实际大小
        double areaX = w + x - btnSize - this.btnMarginRight;
        // 位移的areaY1值
        double areaY1 = 1.5;
        // 位移的areaY2值
        double areaY2 = areaY1 + btnSize + 1.5;
        // 设置按钮位置
        super.positionInArea(this.incrButton, areaX, areaY1, btnSize, btnSize, 0, HPos.CENTER, VPos.CENTER);
        super.positionInArea(this.decrButton, areaX, areaY2, btnSize, btnSize, 0, HPos.CENTER, VPos.CENTER);
    }

    /**
     * 禁用减少值按钮
     */
    public void disableDecrButton() {
        this.decrButton.setDisable(true);
    }

    /**
     * 启用减少值按钮
     */
    public void enableDecrButton() {
        this.decrButton.setDisable(false);
    }

    /**
     * 禁用增加值按钮
     */
    public void disableIncrButton() {
        this.incrButton.setDisable(false);
    }

    /**
     * 启用增加值按钮
     */
    public void enableIncrButton() {
        this.incrButton.setDisable(false);
    }
}
