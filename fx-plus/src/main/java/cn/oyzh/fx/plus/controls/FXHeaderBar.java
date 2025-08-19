package cn.oyzh.fx.plus.controls;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import javafx.scene.layout.HeaderBar;

/**
 * @author oyzh
 * @since 2025-08-19
 */
public class FXHeaderBar extends HeaderBar {

    {
        this.setId("headerBar");
    }

    public FXHBox getContent() {
        if (OSUtil.isMacOS()) {
            return (FXHBox) this.getTrailing();
        }
        return (FXHBox) this.getLeading();
    }

    public void setContent(FXHBox content) {
        if (OSUtil.isMacOS()) {
            this.setTrailing(content);
        } else {
            this.setLeading(content);
        }
    }
}
