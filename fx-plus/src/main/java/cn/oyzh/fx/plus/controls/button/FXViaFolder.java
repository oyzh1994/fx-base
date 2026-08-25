package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Hyperlink;

/**
 * @author oyzh
 * @since 2024-12-23
 */
public class FXViaFolder extends Hyperlink implements LayoutAdapter, MouseAdapter, NodeGroup, NodeAdapter, ThemeAdapter, TipAdapter, StateAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    public FXViaFolder() {
        super();
    }

    public FXViaFolder(String text) {
        super(text);
    }

    @Override
    public void initNode() {
        NodeAdapter.super.initNode();
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setPadding(Insets.EMPTY);
        this.setMnemonicParsing(false);
        this.setOnMousePrimaryClicked(event -> {
            String url = this.getText();
            if (StringUtil.isNotBlank(url)) {
                SystemUtil.openFolderViaCommand(url);
            }
        });
    }
}
