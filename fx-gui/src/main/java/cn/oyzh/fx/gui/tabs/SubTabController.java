package cn.oyzh.fx.gui.tabs;

import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author oyzh
 * @since 2023/10/12
 */
public class SubTabController extends RichTabController {

    /**
     * 父控制器
     */
    @Setter
    @Accessors(fluent = true, chain = false)
    private ParentTabController parent;

    public ParentTabController parent() {
        return this.parent;
    }
}
