package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.flex.FlexAdapter;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FlexButton extends FXButton implements FlexAdapter {

    public FlexButton( ) {
        super();
    }

    public FlexButton(String text) {
        super(text);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
