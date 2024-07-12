package cn.oyzh.fx.plus.controls.choose;

import cn.oyzh.fx.plus.controls.svg.ChooseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * 选择输入框皮肤
 *
 * @author oyzh
 * @since 2023/10/9
 */
public class ChooseTextFieldSkin extends TextFieldSkinExt {

    @Setter
    @Getter
    private Runnable chooseAction;

    /**
     * 清除按钮
     */
    protected final SVGGlyph chooseButton;

    public ChooseTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化清除按钮
        this.chooseButton = new ChooseSVGGlyph();
        this.chooseButton.setEnableWaiting(false);
        this.chooseButton.setFocusTraversable(false);
        this.chooseButton.setTipText(I18nHelper.choose());
        this.chooseButton.setPadding(new Insets(0));
        this.chooseButton.setOnMousePrimaryClicked(event -> {
            if (this.chooseAction != null) {
                this.chooseAction.run();
            }
        });
        this.chooseButton.setOnMouseMoved(mouseEvent -> this.chooseButton.setColor("#DC143C"));
        this.chooseButton.setOnMouseExited(mouseEvent -> this.chooseButton.setColor(this.getButtonColor()));
        this.getChildren().add(this.chooseButton);
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
        // 按钮大小，组件高度/2
        this.chooseButton.setSize(h);
        // 计算组件大小
        double btnSize = this.snapSizeX(h);
        // 位移的areaX值，规则 组件宽+x-按钮大小
        double areaX = w + x - btnSize;
        // 设置位置
        super.positionInArea(this.chooseButton, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
    }
}
