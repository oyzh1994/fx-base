package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.fx.gui.skin.ChooseFileTextFieldSkin;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;

import java.io.File;
import java.util.function.Consumer;

/**
 * 文件选择框
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseFileTextField extends FXTextField {

    @Override
    public byte[] getValue() {
        File file = this.skin().getFile();
        if (file == null) {
            return (byte[]) super.getValue();
        }
        return FileUtil.readBytes(file);
    }

    @Override
    public void setValue(Object val) {
        if (val instanceof byte[] bytes) {
            super.setValue(bytes);
        } else if (val instanceof Byte[] bytes) {
            byte[] data = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                data[i] = bytes[i];
            }
            super.setValue(data);
        }
    }

    public void setFilter(FileExtensionFilter filter) {
        this.skin().setFilter(filter);
    }

    public boolean isAlwaysShowGraphic() {
        return this.skin().isAlwaysShowGraphic();
    }

    public void setAlwaysShowGraphic(boolean alwaysShowGraphic) {
        this.skin().setAlwaysShowGraphic(alwaysShowGraphic);
    }

    public File getFile() {
        return this.skin().getFile();
    }

    @Override
    public ChooseFileTextFieldSkin skin() {
        return (ChooseFileTextFieldSkin) super.skin();
    }

    @Override
    protected ChooseFileTextFieldSkin createDefaultSkin() {
        return new ChooseFileTextFieldSkin(this);
    }

    public void setOnSelectedFile(Consumer<File> onSelectedFile) {
        this.skin().setOnSelectedFile(onSelectedFile);
    }
}
