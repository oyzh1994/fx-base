package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.control.Menu;

/**
 * 菜单
 *
 * @author oyzh
 * @since 2025/07/24
 */
public class FXMenu extends Menu implements StateAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    {
        this.setStyle("-fx-padding: 0 0 0 0;");
        this.disableProperty().addListener((observable, oldValue, newValue) -> {
            if (this.getGraphic() != null) {
                this.getGraphic().setDisable(newValue);
            }
        });
    }

    public FXMenu() {
        super();
    }

    public FXMenu(Node graphic, String text, Runnable action) {
        if (text != null) {
            super.setText(text);
        }
        if (graphic != null) {
            super.setGraphic(graphic);
        }
        if (action != null) {
            super.setOnAction(event -> action.run());
        }
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     */
    public double getWidth() {
        String str = this.getText();
        Node graphic = this.getGraphic();
        double w = FontUtil.stringWidth(str);
        if (graphic != null) {
            w += NodeUtil.getWidth(graphic);
        }
        return w;
    }
}
