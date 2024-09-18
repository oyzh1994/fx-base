package cn.oyzh.fx.plus.tabs;

import cn.oyzh.fx.plus.controller.ParentStageController;
import cn.oyzh.fx.plus.controller.StageController;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class SubTabController extends DynamicTabController {

    /**
     * 父控制器
     */
    @Getter
    @Setter
    @Accessors(fluent = true, chain = false)
    private ParentTabController parent;
}
