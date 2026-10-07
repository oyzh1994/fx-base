package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.ChooseSVGGlyph;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.function.Consumer;

/**
 * 保存文件输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class SaveFileTextFieldSkin extends ActionTextFieldSkin {

    /**
     * 初始文件名
     */
    private String initFileName;

    /**
     * 文件扩展名过滤器
     */
    private FileExtensionFilter extension;

    /**
     * 获取文件选中回调
     *
     * @return 文件选中回调
     */
    public Consumer<File> getOnFileSelected() {
        return onFileSelected;
    }

    /**
     * 设置文件选中回调
     *
     * @param onFileSelected 文件选中回调
     */
    public void setOnFileSelected(Consumer<File> onFileSelected) {
        this.onFileSelected = onFileSelected;
    }

    /**
     * 获取文件扩展名过滤器
     *
     * @return 文件扩展名过滤器
     */
    public FileExtensionFilter getExtension() {
        return extension;
    }

    /**
     * 设置文件扩展名过滤器
     *
     * @param extension 文件扩展名过滤器
     */
    public void setExtension(FileExtensionFilter extension) {
        this.extension = extension;
    }

    /**
     * 获取初始文件名
     *
     * @return 初始文件名
     */
    public String getInitFileName() {
        return initFileName;
    }

    /**
     * 设置初始文件名
     *
     * @param initFileName 初始文件名
     */
    public void setInitFileName(String initFileName) {
        this.initFileName = initFileName;
    }

    /**
     * 文件选中回调
     */
    private Consumer<File> onFileSelected;

    @Override
    protected void onButtonClick(MouseEvent e) {
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

    /**
     * 以指定文本输入框构造保存文件输入框皮肤。
     *
     * @param textField 文本输入框
     */
    public SaveFileTextFieldSkin(TextField textField) {
        super(textField);
        // super(textField, new ChooseSVGGlyph());
        // this.button.disappear();
        // this.button.setTipText(I18nHelper.save());
    }

    @Override
    protected SVGGlyph getButton() {
        if (super.button == null) {
            super.button = new ChooseSVGGlyph();
            super.initButton(super.button);
        }
        return super.button;
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }

    @Override
    public void dispose() {
        this.extension = null;
        this.initFileName = null;
        this.onFileSelected = null;
        super.dispose();
    }
}
