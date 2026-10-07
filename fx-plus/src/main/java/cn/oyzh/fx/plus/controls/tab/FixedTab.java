package cn.oyzh.fx.plus.controls.tab;

/**
 * 固定tab页签，不可关闭
 *
 * @author oyzh
 * @since 2026-06-23
 */
public class FixedTab extends FXTab {

    @Override
    public void initNode() {
        super.setClosable(false);
        super.initNode();
    }
}
