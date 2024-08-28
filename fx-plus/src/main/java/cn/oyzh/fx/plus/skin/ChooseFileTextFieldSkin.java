package cn.oyzh.fx.plus.skin;

import cn.oyzh.fx.plus.controls.svg.ChooseSVGGlyph;
import cn.oyzh.fx.plus.file.FileChooserHelper;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.Getter;

import java.io.File;

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

    @Override
    protected void onButtonClicked(MouseEvent e) {
        File file1 = FileChooserHelper.choose(I18nHelper.chooseFile(), "All", "*.*");
        if (file1 != null) {
            this.file = file1;
            this.setText(this.file.getName());
            this.setTipText(this.file.getPath());
        }
    }

    public ChooseFileTextFieldSkin(TextField textField) {
        super(textField, new ChooseSVGGlyph("13"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.choose());
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }
}
