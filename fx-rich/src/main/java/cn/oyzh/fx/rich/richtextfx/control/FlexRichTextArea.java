package cn.oyzh.fx.rich.richtextfx.control;

import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.util.NodeUtil;

/**
 * @author oyzh
 * @since 2023/9/28
 */
public class FlexRichTextArea extends BaseRichTextArea implements FlexAdapter, AreaAdapter {

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void flushCaret() {
        this.positionCaret(this.getCaretPosition());
        this.requestFocus();
    }

    @Override
    public void setFontSize(double fontSize) {
        NodeUtil.replaceStyle(this, "-fx-font-size", fontSize + "px");
    }
}
