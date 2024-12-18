package cn.oyzh.fx.plus.controls.list;

import cn.oyzh.fx.plus.flex.FlexAdapter;

/**
 * @author oyzh
 * @since 2024/01/24
 */
public class FlexListView<T> extends FXListView<T> implements FlexAdapter {

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

}
