package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2023/3/2
 */
public interface BaseTrayItem {


       DorkboxTrayItem toDorkboxTrayItem();


}
