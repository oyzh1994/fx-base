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
public class ChooseFileTextFieldSkin extends ActionTextFieldSkin {

    /**
     * 文件
     */
    protected File file;

    /**
     * 是否一直显示图标
     */
    protected boolean alwaysShowGraphic;

    /**
     * 过滤器
     */
    protected FileExtensionFilter filter;

    /**
     * 文件选中事件
     */
    private Consumer<File> onSelectedFile;

    public Consumer<File> getOnSelectedFile() {
        return onSelectedFile;
    }

    public void setOnSelectedFile(Consumer<File> onSelectedFile) {
        this.onSelectedFile = onSelectedFile;
    }

    public FileExtensionFilter getFilter() {
        return filter;
    }

    public void setFilter(FileExtensionFilter filter) {
        this.filter = filter;
    }

    public boolean isAlwaysShowGraphic() {
        return alwaysShowGraphic;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    protected void onButtonClicked(MouseEvent e) {
        if (this.filter == null) {
            this.filter = FXChooser.allExtensionFilter();
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
