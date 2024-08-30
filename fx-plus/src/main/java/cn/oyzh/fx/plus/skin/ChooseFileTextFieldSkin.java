package cn.oyzh.fx.plus.skin;

import cn.oyzh.fx.plus.controls.svg.ChooseSVGGlyph;
import cn.oyzh.fx.plus.file.FileChooserHelper;
import cn.oyzh.fx.plus.file.FileExtensionFilter;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.function.Consumer;

/**
 * 文件文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseFileTextFieldSkin extends ActionTextFieldSkinExt {

    /**
     * 文件
     */
    @Getter
    protected File file;

    /**
     * 是否一直显示图标
     */
    @Getter
    protected boolean alwaysShowGraphic;

    /**
     * 过滤器
     */
    @Setter
    @Getter
    protected FileExtensionFilter filter;

    /**
     * 文件选中事件
     */
    @Getter
    @Setter
    private Consumer<File> onSelectedFile;

    @Override
    protected void onButtonClicked(MouseEvent e) {
        if (this.filter == null) {
            this.filter = FileChooserHelper.allExtensionFilter();
        }
        File file1 = FileChooserHelper.choose(I18nHelper.chooseFile(), this.filter);
        if (file1 != null) {
            this.file = file1;
            if (this.onSelectedFile == null) {
                this.setText(this.file.getPath());
                this.setTipText(this.file.getPath());
            } else {
                this.onSelectedFile.accept(this.file);
            }
        }
    }

    public ChooseFileTextFieldSkin(TextField textField) {
        super(textField, new ChooseSVGGlyph("13"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.choose());
    }

    @Override
    protected void updateButtonVisibility() {
        if (this.alwaysShowGraphic) {
            this.button.display();
        } else {
            boolean visible = this.getSkinnable().isVisible();
            boolean disable = this.getSkinnable().isDisable();
            boolean hasFocus = this.getSkinnable().isFocused();
            boolean shouldBeVisible = !disable && visible && hasFocus;
            this.button.setVisible(shouldBeVisible);
        }
    }

    public void setAlwaysShowGraphic(boolean alwaysShowGraphic) {
        this.alwaysShowGraphic = alwaysShowGraphic;
        this.updateButtonVisibility();
    }
}
