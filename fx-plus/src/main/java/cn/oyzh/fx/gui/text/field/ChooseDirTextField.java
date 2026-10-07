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

    /**
     * 设置初始目录
     *
     * @param initDir 初始目录
     */
    public void setInitDir(String initDir) {
        this.skin().setInitDir(initDir);
    }

    /**
     * 是否一直显示图标
     *
     * @return 是否一直显示图标
     */
    public boolean isAlwaysShowGraphic() {
        return this.skin().isAlwaysShowGraphic();
    }

    /**
     * 设置是否一直显示图标
     *
     * @param alwaysShowGraphic 是否一直显示图标
     */
    public void setAlwaysShowGraphic(boolean alwaysShowGraphic) {
        this.skin().setAlwaysShowGraphic(alwaysShowGraphic);
    }

    /**
     * 获取已选目录
     *
     * @return 已选目录
     */
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

    /**
     * 设置目录选中回调
     *
     * @param onSelectedDir 目录选中回调
     */
    public void setOnSelectedDir(Consumer<File> onSelectedDir) {
        this.skin().setOnSelectedDir(onSelectedDir);
    }
}
