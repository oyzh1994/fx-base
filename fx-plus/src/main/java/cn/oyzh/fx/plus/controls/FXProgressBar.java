package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.control.ProgressBar;

/**
 * 进度条控件，继承自 ProgressBar，支持主题、字体、状态、布局等适配
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXProgressBar extends ProgressBar implements FlexAdapter, NodeGroup, NodeAdapter, ThemeAdapter, MouseAdapter, TipAdapter, StateAdapter, LayoutAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    /**
     * 设置进度值（在 FX 线程中执行）
     *
     * @param progress 进度值
     */
    public void progress(double progress) {
        FXUtil.runWait(() -> super.setProgress(progress));
    }
}
