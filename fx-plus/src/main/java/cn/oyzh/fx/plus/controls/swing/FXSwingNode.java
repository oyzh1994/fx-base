package cn.oyzh.fx.plus.controls.swing;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.util.SwingUtil;
import javafx.embed.swing.SwingNode;

import javax.swing.*;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class FXSwingNode extends SwingNode implements FlexAdapter, NodeAdapter, PropAdapter, TipAdapter, FontAdapter {

    public void setSize(double width, double height) {
        this.getContent().setSize((int) width, (int) height);
    }

    public void setWidth(double width) {
        this.getContent().setSize((int) width, (int) this.getHeight());
    }

    public double getWidth() {
        return this.getContent().getWidth();
    }

    public void setHeight(double height) {
        this.getContent().setSize((int) this.getWidth(), (int) height);
    }

    public double getHeight() {
        return this.getContent().getHeight();
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void setContent(JComponent content) {
        SwingUtil.runWait(() -> super.setContent(content));
    }
}
