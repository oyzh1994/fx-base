package cn.oyzh.fx.plus.controls.tab;

/**
 * @author oyzh
 * @since 2022/1/21
 */
public class FixedTab extends FXTab {

    @Override
    public void initNode() {
        super.setClosable(false);
        super.initNode();
    }
}
