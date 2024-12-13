package cn.oyzh.fx.rich.richtextfx.control;

import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;

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

    // public void _scrollToEnd() {
    //     int len = this.getLength() - 1;
    //     if (len > 0) {
    //         this.selectRange(len - 1, len);
    //         this.deselect();
    //         this.positionCaret(this.getLength());
    //     }
    // }

    @Override
    public void flushCaret() {
        this.positionCaret(this.getCaretPosition());
        this.requestFocus();
    }
}
