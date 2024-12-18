package cn.oyzh.fx.plus.controls.text.area;

import cn.oyzh.fx.plus.flex.FlexAdapter;

/**
 * 文本域
 *
 * @author oyzh
 * @since 2022/1/20
 */
public class FlexTextArea extends FXTextArea implements FlexAdapter {

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
