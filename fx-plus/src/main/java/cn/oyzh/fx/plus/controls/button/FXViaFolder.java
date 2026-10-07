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
 * 文件夹打开控件，继承自 Hyperlink，点击后使用系统命令打开文本中的文件夹路径，支持主题、字体、状态等适配
 *
 * @author oyzh
 * @since 2024-12-23
 */
public class FXViaFolder extends Hyperlink implements LayoutAdapter, MouseAdapter, NodeGroup, NodeAdapter, ThemeAdapter, TipAdapter, StateAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造经由文件夹对象。
     */
    public FXViaFolder() {
        super();
    }

    /**
     * 构造经由文件夹对象。
     *
     * @param text 文本
     */
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
