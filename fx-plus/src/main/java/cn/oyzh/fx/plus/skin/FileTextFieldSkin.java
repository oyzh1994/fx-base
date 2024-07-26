package cn.oyzh.fx.plus.skin;

import cn.oyzh.fx.plus.controls.svg.ChooseSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.util.FileChooserUtil;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import lombok.Getter;

import java.io.File;

/**
 * 文件文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class FileTextFieldSkin extends ActionTextFieldSkinExt {

    /**
     * 文件
     */
    @Getter
    protected File file;

    @Override
    protected void onButtonClicked(MouseEvent e) {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("All", "*.*");
        File file1 = FileChooserUtil.choose(I18nHelper.chooseFile(), new FileChooser.ExtensionFilter[]{filter});
        if (file1 != null) {
            this.file = file1;
            this.setText(this.file.getName());
            this.setTipText(this.file.getPath());
        }
    }

    public FileTextFieldSkin(TextField textField) {
        super(textField, new ChooseSVGGlyph("13"));
        // 初始化按钮
        this.button.setTipText(I18nHelper.choose());
    }
}
