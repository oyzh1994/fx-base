package cn.oyzh.fx.plus.controls.swing;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.embed.swing.SwingNode;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class FXSwingNode extends SwingNode implements NodeGroup, ThemeAdapter, FlexAdapter, NodeAdapter, PropAdapter, TipAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    public void setSize(double width, double height) {
        // SwingUtil.runWait(() ->
        this.getContent().setSize((int) width, (int) height);
        // );
    }

    public void setWidth(double width) {
        this.setSize(width, this.getHeight());
    }

    public double getWidth() {
        // AtomicReference<Integer> ref = new AtomicReference<>();
        // SwingUtil.runWait(() -> {
        //     int val = this.getContent().getWidth();
        //     ref.set(val);
        // });
        // return ref.get();
        return this.getContent().getWidth();
    }

    public void setHeight(double height) {
        this.setSize(this.getWidth(), height);
    }

    public double getHeight() {
        // AtomicReference<Integer> ref = new AtomicReference<>();
        // SwingUtil.runWait(() -> {
        //     int val = this.getContent().getHeight();
        //     ref.set(val);
        // });
        // return ref.get();
        return this.getContent().getHeight();
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
    }
}
