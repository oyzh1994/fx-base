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
public class ParentTabController extends RichTabController {

    @Override
    protected void setTab(RichTab tab) {
        super.setTab(tab);
        for (RichTabController controller : this.getSubControllers()) {
            if (controller instanceof SubTabController subTabController) {
                subTabController.parent(this);
            }
            controller.setTab(tab);
        }
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        for (RichTabController controller : this.getSubControllers()) {
            controller.onTabInit(tab);
        }
    }

    @Override
    public void onTabClose(RichTab tab, Event event) {
        super.onTabClose(tab, event);
        for (RichTabController controller : this.getSubControllers()) {
            controller.onTabClose(tab, event);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        for (RichTabController controller : this.getSubControllers()) {
            controller.initialize(location, resourceBundle);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        for (RichTabController controller : this.getSubControllers()) {
            controller.bindListeners();
        }
    }

    /**
     * 获取子控制器列表
     *
     * @return 子控制器列表
     */
    public List<? extends RichTabController> getSubControllers() {
        return Collections.emptyList();
    }
}
