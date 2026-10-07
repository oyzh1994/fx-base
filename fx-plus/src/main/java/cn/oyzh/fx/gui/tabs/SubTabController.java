package cn.oyzh.fx.gui.tabs;


/**
 * 子标签页控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class SubTabController extends RichTabController {

    /**
     * 父控制器
     */
    private ParentTabController parent;

    /**
     * 获取父控制器
     *
     * @return 父控制器
     */
    public ParentTabController parent() {
        return this.parent;
    }

    /**
     * 设置父控制器
     *
     * @param parent 父控制器
     */
    public void parent(ParentTabController parent) {
        this.parent = parent;
    }
}
