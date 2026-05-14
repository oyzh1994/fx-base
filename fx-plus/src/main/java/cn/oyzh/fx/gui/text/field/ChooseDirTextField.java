package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.ChooseDirTextFieldSkin;
import javafx.scene.control.Skin;

import java.io.File;
import java.util.function.Consumer;

/**
 * 目录选择框
 *
 * @author oyzh
 * @since 2026/01/04
 */
public class ChooseDirTextField extends ClearableTextField {

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ChooseDirTextFieldSkin skin() {
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return (ChooseDirTextFieldSkin) this.getSkin();
    }

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
    protected Skin<?> createDefaultSkin() {
        return new ChooseDirTextFieldSkin(this);
    }

    public void setOnSelectedDir(Consumer<File> onSelectedDir) {
        this.skin().setOnSelectedDir(onSelectedDir);
    }
}
