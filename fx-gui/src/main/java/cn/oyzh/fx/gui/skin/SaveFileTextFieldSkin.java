package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.ChooseSVGGlyph;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.function.Consumer;

/**
 * 文件文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class SaveFileTextFieldSkin extends ActionTextFieldSkin {

    private String initFileName;

    private FileExtensionFilter extension;

    public Consumer<File> getOnFileSelected() {
        return onFileSelected;
    }

    public void setOnFileSelected(Consumer<File> onFileSelected) {
        this.onFileSelected = onFileSelected;
    }

    public FileExtensionFilter getExtension() {
        return extension;
    }

    public void setExtension(FileExtensionFilter extension) {
        this.extension = extension;
    }

    public String getInitFileName() {
        return initFileName;
    }

    public void setInitFileName(String initFileName) {
        this.initFileName = initFileName;
    }

    private Consumer<File> onFileSelected;

    @Override
    protected void onButtonClicked(MouseEvent e) {
        if (this.extension == null) {
            this.extension = FXChooser.allExtensionFilter();
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
