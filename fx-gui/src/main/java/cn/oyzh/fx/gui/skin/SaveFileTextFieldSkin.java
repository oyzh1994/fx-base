package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.ChooseSVGGlyph;
import cn.oyzh.fx.plus.file.FileChooserHelper;
import cn.oyzh.fx.plus.file.FileExtensionFilter;
import cn.oyzh.fx.plus.skin.ActionTextFieldSkinExt;
import cn.oyzh.i18n.I18nHelper;
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
public class SaveFileTextFieldSkin extends ActionTextFieldSkinExt {

    @Getter
    @Setter
    private String initFileName;

    @Getter
    @Setter
    private FileExtensionFilter extension;

    @Getter
    @Setter
    private Consumer<File> onFileSelected;

    @Override
    protected void onButtonClicked(MouseEvent e) {
        if (this.extension == null) {
            this.extension = FileChooserHelper.allExtensionFilter();
        }
        File file1 = FileChooserHelper.save(I18nHelper.chooseFile(), this.initFileName, this.extension);
        if (file1 != null) {
            if (this.onFileSelected != null) {
                this.onFileSelected.accept(file1);
            } else {
                this.setText(file1.getPath());
                this.setTipText(file1.getPath());
            }
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
