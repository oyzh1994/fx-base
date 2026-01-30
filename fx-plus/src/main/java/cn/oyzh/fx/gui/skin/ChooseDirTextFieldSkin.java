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

    public Consumer<File> getOnSelectedDir() {
        return onSelectedDir;
    }

    public void setOnSelectedDir(Consumer<File> onSelectedDir) {
        this.onSelectedDir = onSelectedDir;
    }

    public String getInitDir() {
        return initDir;
    }

    public void setInitDir(String initDir) {
        this.initDir = initDir;
    }

    public boolean isAlwaysShowGraphic() {
        return alwaysShowGraphic;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    @Override
    protected void onButtonClicked(MouseEvent e) {
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
