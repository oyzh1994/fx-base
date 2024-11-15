package cn.oyzh.fx.plus.gui.button;


import cn.oyzh.fx.plus.controls.button.IconButton;

/**
 * @author oyzh
 * @since 2024/04/09
 */
public class AccentButton extends IconButton {

    @Override
    public void initNode() {
        this.addClass("accent");
        super.initNode();
    }
}
