package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.function.Consumer;

/**
 * 目录文本输入框皮肤
 *
 * @author oyzh
 * @since 2026/01/04
 */
public class ChooseDirTextFieldSkin extends ChooseTextFieldSkin {

    /**
     * 目录
     */
    protected File dir;

    /**
     * 是否一直显示图标
     */
    protected boolean alwaysShowGraphic;

    /**
     * 初始目录
     */
    protected String initDir;

    /**
     * 目录选中事件
     */
    private Consumer<File> onSelectedDir;

    /**
     * 获取选中目录。
     *
     * @return 选中目录
     */
    public Consumer<File> getOnSelectedDir() {
        return onSelectedDir;
    }

    /**
     * 设置选中目录。
     *
     * @param onSelectedDir 选中目录
     */
    public void setOnSelectedDir(Consumer<File> onSelectedDir) {
        this.onSelectedDir = onSelectedDir;
    }

    /**
     * 获取初始目录。
     *
     * @return 初始目录
     */
    public String getInitDir() {
        return initDir;
    }

    /**
     * 设置初始目录。
     *
     * @param initDir 初始目录
     */
    public void setInitDir(String initDir) {
        this.initDir = initDir;
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
     * 获取目录。
     *
     * @return 目录
     */
    public File getDir() {
        return dir;
    }

    /**
     * 设置目录。
     *
     * @param dir 目录
     */
    public void setDir(File dir) {
        this.dir = dir;
    }

    @Override
    protected void onButtonClick(MouseEvent e) {
        File file1 = DirChooserHelper.choose(I18nHelper.chooseFile(), this.initDir, StageManager.getFrontWindow());
        if (file1 != null) {
            this.dir = file1;
            if (this.onSelectedDir == null) {
                this.setText(this.dir.getPath());
                this.setTipText(this.dir.getPath());
            } else {
                this.onSelectedDir.accept(this.dir);
            }
        }
    }

    /**
     * 构造选择目录文本字段皮肤对象。
     *
     * @param textField 文本框
     */
    public ChooseDirTextFieldSkin(TextField textField) {
        super(textField);
    }

    @Override
    protected void updateButtonVisibility() {
        if (this.alwaysShowGraphic) {
            this.button.display();
        } else {
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
        this.dir = null;
        this.initDir = null;
        this.onSelectedDir = null;
        super.dispose();
    }
}
