package cn.oyzh.fx.plus.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 子控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class SubController extends Controller {

    /**
     * 父控制器
     */
    @Getter
    @Setter
    @Accessors(fluent = true, chain = false)
    private ParentController parent;
}
