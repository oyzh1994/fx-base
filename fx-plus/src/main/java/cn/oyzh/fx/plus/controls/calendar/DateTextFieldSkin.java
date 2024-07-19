package cn.oyzh.fx.plus.controls.calendar;

import atlantafx.base.controls.Calendar;
import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.window.PopupExt;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * 选择输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class DateTextFieldSkin extends TextFieldSkinExt {

    /**
     * 选择事件
     */
    @Setter
    @Getter
    private Runnable chooseAction;

    /**
     * 选择按钮
     */
    protected final SVGGlyph chooseButton;

    private PopupExt popup;

    public void showDatePopup() {
        this.popup = new PopupExt();
        this.popup.setAutoFix(true);
        this.popup.setAnimated(true);
        this.popup.setAutoHide(true);
        this.popup.setHideOnEscape(true);
        this.popup.setFadeInDuration(Duration.millis(600));
        this.popup.setFadeOutDuration(Duration.millis(600));
        // 日期组件
        Calendar calendar = new Calendar();
        this.popup.setContentNode(calendar);
        this.popup.showPopup(this.getSkinnable());
    }

    public DateTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化清除按钮
        this.chooseButton = new SVGGlyph("/fx-plus/font/date.svg", "14");
        this.chooseButton.setEnableWaiting(false);
        this.chooseButton.setFocusTraversable(false);
        this.chooseButton.setTipText(I18nHelper.choose());
        this.chooseButton.setPadding(new Insets(0));
        this.chooseButton.setOnMousePrimaryClicked(event -> {
            // if (this.chooseAction != null) {
            //     this.chooseAction.run();
            // }
            this.showDatePopup();
        });
        this.chooseButton.setOnMouseMoved(mouseEvent -> this.chooseButton.setColor("#DC143C"));
        this.chooseButton.setOnMouseExited(mouseEvent -> this.resetButtonColor());
        this.getChildren().add(this.chooseButton);
    }

    public void resetButtonColor() {
        this.chooseButton.setColor(this.getButtonColor());
    }

    @Override
    protected Color getButtonColor() {
        if (!ThemeManager.isDarkMode()) {
            return Color.valueOf("#696969");
        }
        return super.getButtonColor();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // 计算组件大小
        double btnSize = this.snapSizeX(h * 0.7);
        // 限制最大最小值
        btnSize = NumUtil.limit(btnSize, 14, 20);
        // 按钮大小，组件高度
        this.chooseButton.setSize(btnSize);
        // 位移的areaX值，规则 组件宽+x-按钮大小
        double areaX = w + x - btnSize - 8;
        // 设置位置
        super.positionInArea(this.chooseButton, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
    }
}
