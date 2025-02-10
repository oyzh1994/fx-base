package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.fx.plus.flex.FlexAdapter;

/**
 * @author oyzh
 * @since 2022/1/20
 */
public class FlexFlowPane extends FXFlowPane implements FlexAdapter {

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
