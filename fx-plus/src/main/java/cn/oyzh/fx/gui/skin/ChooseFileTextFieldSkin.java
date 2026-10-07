package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 文件文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseFileTextFieldSkin extends ChooseTextFieldSkin {

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
    protected List<FileExtensionFilter> filters;

    /**
     * 文件选中事件
     */
    private Consumer<File> onSelectedFile;

    /**
     * 获取选中文件。
     *
     * @return 选中文件
     */
    public Consumer<File> getOnSelectedFile() {
        return onSelectedFile;
    }

    /**
     * 设置选中文件。
     *
     * @param onSelectedFile 选中文件
     */
    public void setOnSelectedFile(Consumer<File> onSelectedFile) {
        this.onSelectedFile = onSelectedFile;
    }

    /**
     * 获取过滤器集合。
     *
     * @return 过滤器集合
     */
    public List<FileExtensionFilter> getFilters() {
        return filters;
    }

    /**
     * 设置过滤器集合。
     *
     * @param filters 过滤器集合
     */
    public void setFilters(List<FileExtensionFilter> filters) {
        this.filters = filters;
    }

    /**
     * 是否总是显示。
     *
     * @return 总是显示
     */
    public boolean isAlwaysShowGraphic() {
        return alwaysShowGraphic;
    }

    /**
     * 获取文件。
     *
     * @return 文件
     */
    public File getFile() {
        return file;
    }

    /**
     * 设置文件。
     *
     * @param file 文件
     */
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    protected void onButtonClick(MouseEvent e) {
        if (this.filters == null||this.filters.isEmpty()) {
            this.filters = new ArrayList<>();
            this.filters.add(FXChooser.allExtensionFilter());
        }
        File file1 = FileChooserHelper.choose(I18nHelper.chooseFile(), this.filters);
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

    /**
     * 构造选择文件文本字段皮肤对象。
     *
     * @param textField 文本框
     */
    public ChooseFileTextFieldSkin(TextField textField) {
        super(textField);
    }

    @Override
    protected void updateButtonVisibility() {
        if (this.alwaysShowGraphic) {
            this.button.display();
        } else {
            // boolean visible = this.getSkinnable().isVisible();
            // boolean disable = this.getSkinnable().isDisable();
            // boolean hasFocus = this.getSkinnable().isFocused();
            // boolean shouldBeVisible = !disable && visible && hasFocus;
            // this.button.setVisible(shouldBeVisible);
            super.updateButtonVisibility();
        }
    }

    /**
     * 设置是否一直显示图标
     *
     * @param alwaysShowGraphic 是否一直显示图标
     */
    public void setAlwaysShowGraphic(boolean alwaysShowGraphic) {
        this.alwaysShowGraphic = alwaysShowGraphic;
        this.updateButtonVisibility();
    }

    @Override
    public void dispose() {
        this.file = null;
        this.filters = null;
        this.onSelectedFile = null;
        super.dispose();
    }
}
