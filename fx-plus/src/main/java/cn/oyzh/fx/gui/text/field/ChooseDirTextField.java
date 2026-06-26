package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.ChooseDirTextFieldSkin;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;

import java.io.File;
import java.util.function.Consumer;

/**
 * 目录选择框
 *
 * @author oyzh
 * @since 2026/01/04
 */
public class ChooseDirTextField extends FXTextField {

    public void setInitDir(String initDir) {
        this.skin().setInitDir(initDir);
    }

    public boolean isAlwaysShowGraphic() {
        return this.skin().isAlwaysShowGraphic();
    }

    public void setAlwaysShowGraphic(boolean alwaysShowGraphic) {
        this.skin().setAlwaysShowGraphic(alwaysShowGraphic);
    }

    public File getDir() {
        return this.skin().getDir();
    }

    @Override
    public ChooseDirTextFieldSkin skin() {
        return (ChooseDirTextFieldSkin) super.skin();
    }

    @Override
    protected ChooseDirTextFieldSkin createDefaultSkin() {
        return new ChooseDirTextFieldSkin(this);
    }

    public void setOnSelectedDir(Consumer<File> onSelectedDir) {
        this.skin().setOnSelectedDir(onSelectedDir);
    }
}
