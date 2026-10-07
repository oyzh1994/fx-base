package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 文件按钮
 *
 * @author oyzh
 * @since 2024-04-22
 */
public class FileButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.setText(I18nHelper.file());
        this.setTipText(I18nHelper.file());
        this.init(new FileSVGGlyph());
        super.initNode();
    }
}
