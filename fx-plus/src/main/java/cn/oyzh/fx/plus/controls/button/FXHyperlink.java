package cn.oyzh.fx.plus.controls.button;

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
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Hyperlink;

/**
 * 超链接控件，继承自 Hyperlink，点击后使用浏览器打开文本中的地址，支持主题、字体、状态等适配
 *
 * @author oyzh
 * @since 2024-12-23
 */
public class FXHyperlink extends Hyperlink implements LayoutAdapter, MouseAdapter, NodeGroup, NodeAdapter, ThemeAdapter, TipAdapter, StateAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造超链接对象。
     */
    public FXHyperlink() {
        super();
    }

    /**
     * 构造超链接对象。
     *
     * @param text 文本
     */
    public FXHyperlink(String text) {
        super(text);
    }

    @Override
    public void initNode() {
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setPadding(Insets.EMPTY);
        this.setMnemonicParsing(false);
//        this.setFocusTraversable(false);
        this.setOnMousePrimaryClicked(event -> {
            String url = this.getText();
            if (StringUtil.isNotBlank(url)) {
                FXUtil.showDocument(url);
            }
        });
        NodeAdapter.super.initNode();
    }
}
