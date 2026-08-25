package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.SaveFileTextFieldSkin;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import javafx.scene.control.Skin;

import java.io.File;
import java.util.function.Consumer;

/**
 * 文件保存框
 *
 * @author oyzh
 * @since 2024/08/27
 */
public class SaveFileTextField extends LimitTextField {

    public void setInitFileName(String initFileName) {
        this.skin().setInitFileName(initFileName);
    }

    public void setExtension(FileExtensionFilter extension) {
        this.skin().setExtension(extension);
    }

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
