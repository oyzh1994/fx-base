package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.Accordion;

/**
 * 手风琴控件，继承自 Accordion，支持主题、字体、状态等适配
 *
 * @author oyzh
 * @since 2025-11-17
 */
public class FXAccordion extends Accordion implements FlexAdapter, NodeGroup, ThemeAdapter, FontAdapter, StateAdapter, NodeAdapter, LayoutAdapter {

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
