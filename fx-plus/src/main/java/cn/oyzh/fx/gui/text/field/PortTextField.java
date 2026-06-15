package cn.oyzh.fx.gui.text.field;

/**
 * @author oyzh
 * @since 2022/12/23
 */
public class PortTextField extends NumberTextField {

    @Override
    public void initNode() {
        this.setMin(1L);
        this.setMax(65_535L);
        this.setTipText("1-65535");
        super.initNode();
    }
}
