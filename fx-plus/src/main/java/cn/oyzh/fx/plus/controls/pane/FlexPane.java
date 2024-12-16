package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.layout.Pane;
import javafx.scene.text.FontWeight;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FlexPane extends Pane implements NodeAdapter, ThemeAdapter, FontAdapter, FlexAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void initNode() {

    }
}
