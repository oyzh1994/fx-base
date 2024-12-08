package cn.oyzh.fx.gui.textfield;

/**
 * @author oyzh
 * @since 2022/12/23
 */
public class PortTextField extends NumberTextField {

    public PortTextField() {
        super(false);
        this.setMin(1L);
        this.setMax(65_535L);
        this.setTipText("1-65535");
    }

}
