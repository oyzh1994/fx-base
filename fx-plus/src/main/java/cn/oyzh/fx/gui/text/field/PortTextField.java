package cn.oyzh.fx.gui.text.field;

/**
 * 端口文本输入框，取值范围 1-65535
 *
 * @author oyzh
 * @since 2023-10-09
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
