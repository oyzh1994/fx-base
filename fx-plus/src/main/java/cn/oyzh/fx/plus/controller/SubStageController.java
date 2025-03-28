package cn.oyzh.fx.plus.controller;


/**
 * 子窗口控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class SubStageController extends StageController {

    /**
     * 父控制器
     */
    private ParentStageController parent;

    public ParentStageController getParent() {
        return parent;
    }

    public void setParent(ParentStageController parent) {
        this.parent = parent;
    }
}
