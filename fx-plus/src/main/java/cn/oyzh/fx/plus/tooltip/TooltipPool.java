package cn.oyzh.fx.plus.tooltip;

import cn.oyzh.common.util.Pool;
import javafx.scene.control.Tooltip;

/**
 * @author oyzh
 * @since 2025-07-04
 */
public class TooltipPool extends Pool<Tooltip> {

    public TooltipPool() {
        super(1, 3);
    }

    @Override
    public void returnObject(Tooltip tooltip) {
        if (tooltip == null) {
            return;
        }
        tooltip.setText(null);
        super.returnObject(tooltip);
    }

    @Override
    protected Tooltip newObject() {
        Tooltip tooltip = new Tooltip();
        TooltipUtil.initStyle(tooltip);
        return tooltip;
    }
}
