package cn.oyzh.fx.plus.tree;

import cn.oyzh.fx.plus.controls.FlexVBox;
import javafx.scene.Node;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2023/12/5
 */
public class AvdTreeView extends FlexVBox {

    @Getter
    @Setter
    private double fixedCellSize = 18;

    public void setRoot(AvdTreeItem<?> item) {
        this.setChild(item);
        if (item != null) {
            item.setRealHeight(this.fixedCellSize);
        }
    }

    public AvdTreeItem<?> getRoot() {
        return (AvdTreeItem<?>) this.getChild(0);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
    }
}
