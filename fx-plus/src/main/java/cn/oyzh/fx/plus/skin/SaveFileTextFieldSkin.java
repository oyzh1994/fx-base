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

/**
 * 文件文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class SaveFileTextFieldSkin extends ActionTextFieldSkinExt {

    @Getter
    @Setter
    private FileExtensionFilter extension;

    @Getter
    @Setter
    private String initFileName;

    @Override
    protected void onButtonClicked(MouseEvent e) {
        if (this.extension == null) {
            this.extension = new FileExtensionFilter("All", "*.*");
        }
        File file1 = FileChooserHelper.save(I18nHelper.chooseFile(), this.initFileName, this.extension);
        if (file1 != null) {
            this.setText(file1.getPath());
            this.setTipText(file1.getPath());
        }
    }

    public SaveFileTextFieldSkin(TextField textField) {
        super(textField, new ChooseSVGGlyph("13"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.save());
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
