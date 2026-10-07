package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.plus.controls.button.IconButton;

/**
 * 成功按钮
 *
 * @author oyzh
 * @since 2024-04-09
 */
public class SuccessButton extends IconButton {

    @Override
    public void initNode() {
        this.addClass("success");
        super.initNode();
    }
}
