package cn.oyzh.fx.plus.controls.popup;

import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.stage.Popup;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class FXPopup extends Popup implements ThemeAdapter {

    {
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
//                this.changeTheme(ThemeManager.currentTheme());
                NodeManager.init(this);
            }
        });
    }

}
