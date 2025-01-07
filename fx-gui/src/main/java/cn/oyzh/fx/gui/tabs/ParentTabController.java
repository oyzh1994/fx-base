package cn.oyzh.fx.gui.tabs;

import javafx.event.Event;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 父窗口控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class ParentTabController extends DynamicTabController {

    @Override
    protected void setTab(DynamicTab tab) {
        super.setTab(tab);
        for (DynamicTabController controller : this.getSubControllers()) {
            controller.setTab(tab);
        }
    }

    @Override
    public void onTabInit(DynamicTab tab) {
        super.onTabInit(tab);
        for (DynamicTabController controller : this.getSubControllers()) {
            controller.onTabInit(tab);
        }
    }

    @Override
    public void onTabClose(DynamicTab tab, Event event) {
        super.onTabClose(tab, event);
        for (DynamicTabController controller : this.getSubControllers()) {
            controller.onTabClose(tab, event);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        for (DynamicTabController controller : this.getSubControllers()) {
            controller.initialize(location, resourceBundle);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        for (DynamicTabController controller : this.getSubControllers()) {
            controller.bindListeners();
        }
    }

    /**
     * 获取子控制器列表
     *
     * @return 子控制器列表
     */
    public List<? extends DynamicTabController> getSubControllers() {
        return Collections.emptyList();
    }
}
