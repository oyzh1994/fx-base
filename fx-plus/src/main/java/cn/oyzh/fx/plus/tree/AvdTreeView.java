package cn.oyzh.fx.plus.tree;

import cn.oyzh.fx.plus.controls.FXVBox;
import cn.oyzh.fx.plus.controls.FlexScrollPane;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.control.skin.TreeViewSkin;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2023/12/5
 */
public class AvdTreeView extends FlexScrollPane {

    @Getter
    @Setter
    private double fixedCellSize = 18;

    {
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        //
        // for (Node node : this.lookupAll(".scroll-bar")) {
        //     ScrollBar s = (ScrollBar) node;
        //     s.setSkin(new AvdTreeViewScrollBarSkin(s));
        // }
        this.setVmax(10000);
        this.setVmin(10000);

        this.setSkin(new ScrollPaneSkin(this));
        FXVBox content = new FXVBox();
        this.setContent(content);
    }

    public FXVBox content() {
        return (FXVBox) super.getContent();
    }

    public void setRoot(AvdTreeItem<?> item) {
        this.content().setChild(item);
        if (item != null) {
            item.setRealHeight(this.fixedCellSize);
        }
    }

    public AvdTreeItem<?> getRoot() {
        return (AvdTreeItem<?>) this.content().getChild(0);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }
}
