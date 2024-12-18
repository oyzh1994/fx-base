package cn.oyzh.fx.plus.controls.combo;

import cn.oyzh.fx.plus.flex.FlexAdapter;

/**
 * @author oyzh
 * @since 2022/2/7
 */
public class FlexComboBox<T> extends FXComboBox<T> implements FlexAdapter {

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

}
