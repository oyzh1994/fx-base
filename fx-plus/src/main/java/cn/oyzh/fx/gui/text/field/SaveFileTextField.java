package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.SaveFileTextFieldSkin;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import javafx.scene.control.Skin;

import java.io.File;
import java.util.function.Consumer;

/**
 * 文件保存输入框
 *
 * @author oyzh
 * @since 2024-08-27
 */
public class SaveFileTextField extends LimitTextField {

    /**
     * 设置初始文件名
     *
     * @param initFileName 初始文件名
     */
    public void setInitFileName(String initFileName) {
        this.skin().setInitFileName(initFileName);
    }

    /**
     * 设置文件扩展名过滤器
     *
     * @param extension 文件扩展名过滤器
     */
    public void setExtension(FileExtensionFilter extension) {
        this.skin().setExtension(extension);
    }

    /**
     * 设置文件选中回调
     *
     * @param onSelectedFile 文件选中回调
     */
    public void setOnSelectedFile(Consumer<File> onSelectedFile) {
        this.skin().setOnFileSelected(onSelectedFile);
    }

    @Override
    public SaveFileTextFieldSkin skin() {
        return (SaveFileTextFieldSkin) super.skin();
    }

    @Override
    protected SaveFileTextFieldSkin createDefaultSkin() {
        return new SaveFileTextFieldSkin(this);
    }

    @Override
    public void initNode() {
        this.setEditable(false);
        super.initNode();
    }
}
