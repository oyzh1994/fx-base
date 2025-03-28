package cn.oyzh.fx.gui.tabs;


/**
 * @author oyzh
 * @since 2023/10/12
 */
public class SubTabController extends RichTabController {

    /**
     * 父控制器
     */
    private ParentTabController parent;

    public ParentTabController parent() {
        return this.parent;
    }

    public void parent(ParentTabController parent) {
        this.parent = parent;
    }
}
