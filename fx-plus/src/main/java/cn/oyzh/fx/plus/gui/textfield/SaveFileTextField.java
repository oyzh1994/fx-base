package cn.oyzh.fx.plus.gui.textfield;

import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
import cn.oyzh.fx.plus.file.FileExtensionFilter;
import cn.oyzh.fx.plus.skin.SaveFileTextFieldSkin;
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

    {
        this.setEditable(false);
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public SaveFileTextFieldSkin skin() {
        SaveFileTextFieldSkin skin = (SaveFileTextFieldSkin) this.getSkin();
        if (skin == null) {
            this.setSkin(this.createDefaultSkin());
            skin = (SaveFileTextFieldSkin) this.getSkin();
        }
        return skin;
    }

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
    protected Skin<?> createDefaultSkin() {
        return new SaveFileTextFieldSkin(this);
    }
}
