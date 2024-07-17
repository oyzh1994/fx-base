package cn.oyzh.fx.plus.controls.file;

import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.plus.controls.svg.ChooseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
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
 * @since 2024/07/04
 */
public class FileTextFieldSkin extends TextFieldSkinExt {

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
    protected void onChooseButtonClick() {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("All", "*.*");
        File file1 = FileChooserUtil.choose(I18nHelper.chooseFile(), new FileChooser.ExtensionFilter[]{filter});
        if (file1 != null) {
            this.file = file1;
            this.setText(this.file.getName());
            this.setTipText(this.file.getPath());
        }
    }

    public FileTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化按钮
        this.chooseButton = new ChooseSVGGlyph();
        this.chooseButton.setTipText(I18nHelper.choose());
        this.chooseButton.setEnableWaiting(false);
        this.chooseButton.setFocusTraversable(false);
        this.chooseButton.setPadding(new Insets(0));
        this.chooseButton.setOnMousePrimaryClicked(e -> this.onChooseButtonClick());
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
