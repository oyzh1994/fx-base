package cn.oyzh.fx.gui.treeView;

import cn.oyzh.common.util.Destroyable;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.treeView.FXTreeItemValue;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.experimental.Accessors;


/**
 * 富功能树节点值
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeItemValue extends FXTreeItemValue {

    public RichTreeItemValue() {
        super();
    }

    public RichTreeItemValue(RichTreeItem<?> item) {
        super(item);
    }
}
