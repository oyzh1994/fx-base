package cn.oyzh.fx.plus.controls.text;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.Separator;

/**
 * @author oyzh
 * @since 2023/4/4
 */
public class FXSeparator extends Separator implements FlexAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
