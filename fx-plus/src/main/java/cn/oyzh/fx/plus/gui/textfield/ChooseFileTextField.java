package cn.oyzh.fx.plus.gui.textfield;

import cn.oyzh.common.util.FileUtil;
import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
import cn.oyzh.fx.plus.file.FileExtensionFilter;
import cn.oyzh.fx.plus.skin.ChooseFileTextFieldSkin;
import javafx.scene.control.Skin;

import java.io.File;
import java.util.function.Consumer;

/**
 * 文件选择框
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseFileTextField extends LimitTextField {

    {
        this.setEditable(false);
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ChooseFileTextFieldSkin skin() {
        ChooseFileTextFieldSkin skin = (ChooseFileTextFieldSkin) this.getSkin();
        if (skin == null) {
            this.setSkin(this.createDefaultSkin());
            skin = (ChooseFileTextFieldSkin) this.getSkin();
        }
        return skin;
    }

    /**
     * 数据
     */
    private byte[] data;

    public byte[] getData() {
        File file = this.skin().getFile();
        if (file == null) {
            data = new byte[]{};
        } else {
            data = FileUtil.readBytes(file);
        }
        return data;
    }

    public void setData(Object val) {
        if (val instanceof byte[] bytes) {
            this.data = bytes;
        } else if (val instanceof Byte[] bytes) {
            this.data = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                this.data[i] = bytes[i];
            }
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
    protected Skin<?> createDefaultSkin() {
        return new ChooseFileTextFieldSkin(this);
    }

    public void setOnSelectedFile(Consumer<File> onSelectedFile) {
        this.skin().setOnSelectedFile(onSelectedFile);
    }
}
