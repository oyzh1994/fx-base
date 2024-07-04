package cn.oyzh.fx.plus.controls.file;

import cn.oyzh.fx.plus.controls.svg.ChooseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.ClearableTextFieldSkin;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FileChooserUtil;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import lombok.Getter;

import java.io.File;

/**
 * 文件文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/0704
 */
public class FileTextFieldSkin extends ClearableTextFieldSkin {

    /**
     * 文件
     */
    @Getter
    protected File file;

    /**
     * 文件选择按钮
     */
    protected final SVGGlyph chooseButton;

    /**
     * 显示历史弹窗组件
     */
    protected void showHistoryPopup() {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("All", "*.*");
        this.file = FileChooserUtil.choose(I18nHelper.chooseFile(), new FileChooser.ExtensionFilter[]{filter});
        this.setText(this.file == null ? null : this.file.getName());
        this.setTipText(this.file == null ? null : this.file.getPath());
    }

    public FileTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化历史按钮
        this.chooseButton = new ChooseSVGGlyph();
        this.chooseButton.setTipText(I18nHelper.choose());
        this.chooseButton.setEnableWaiting(false);
        this.chooseButton.setFocusTraversable(false);
        this.chooseButton.setPadding(new Insets(0));
        this.chooseButton.setOnMousePrimaryClicked(e -> this.showHistoryPopup());
        this.chooseButton.setOnMouseMoved(mouseEvent -> this.chooseButton.setColor("#E36413"));
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
        // 组件大小
        double size = h * .8;
        // 计算组件大小
        double btnSize = this.snapSizeX(size);
        // 设置组件大小
        this.chooseButton.setSize(size);
        // 获取边距
        Insets padding = this.getSkinnable().getPadding();
        // 计算左边距
        double paddingLeft = btnSize + 8;
        // 设置左边距
        if (padding.getLeft() != paddingLeft) {
            padding = new Insets(padding.getTop(), padding.getRight(), padding.getBottom(), paddingLeft);
            this.getSkinnable().setPadding(padding);
        }
        // 设置组件位置
        super.positionInArea(this.chooseButton, 3, y * 0.9, w, h, btnSize, HPos.LEFT, VPos.CENTER);
    }
}
