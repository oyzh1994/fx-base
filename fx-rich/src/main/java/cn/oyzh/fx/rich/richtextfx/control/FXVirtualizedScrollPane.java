package cn.oyzh.fx.rich.richtextfx.control;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.scene.layout.Region;
import org.fxmisc.flowless.Virtualized;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * @author oyzh
 * @since 2023/10/7
 */
@DefaultProperty("content")
public class FXVirtualizedScrollPane<V extends Region & Virtualized> extends VirtualizedScrollPane<V> implements FlexAdapter {

    {
        NodeManager.init(this);
    }

    public FXVirtualizedScrollPane(@NamedArg("content") V content) {
        super(content);
        content.prefWidthProperty().bind(this.widthProperty());
        content.prefHeightProperty().bind(this.heightProperty());
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
    }
}
