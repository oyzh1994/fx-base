package cn.oyzh.fx.plus.tree;

import cn.oyzh.fx.plus.controls.FlexHBox;
import cn.oyzh.fx.plus.controls.FlexVBox;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.controls.text.FlexText;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.Objects;

/**
 * @author oyzh
 * @since 2023/12/5
 */
public class AvdTreeItem<E> extends FlexVBox {

    {
        this.setCursor(Cursor.HAND);
    }

    @Getter
    private final byte level;

    @Getter
    private final AvdTreeItem<?> itemParent;

    @Getter
    private final AvdTreeView treeView;

    private AvdTreeItemList<AvdTreeItem<?>> childItem;

    public AvdTreeItemList<AvdTreeItem<?>> getChildItem() {
        if (this.childItem == null) {
            this.childItem = new AvdTreeItemList<>();
        }
        return this.childItem;
    }

    public AvdTreeItem(AvdTreeView treeView, AvdTreeItem<?> itemParent, String name, Node icon) {
        this(treeView, itemParent);
        this.setIcon(icon);
        this.setName(name);
    }

    public AvdTreeItem(AvdTreeView treeView, AvdTreeItem<?> itemParent) {
        this.treeView = treeView;
        this.itemParent = itemParent;
        if (itemParent == null) {
            this.level = 0;
        } else {
            this.level = (byte) (itemParent.level + 1);
        }
        this.setRealHeight(treeView.getFixedCellSize());
    }

    private SimpleObjectProperty<Node> iconProperty;

    public SimpleObjectProperty<Node> iconProperty() {
        if (this.iconProperty == null) {
            this.iconProperty = new SimpleObjectProperty<>();
        }
        return this.iconProperty;
    }

    public void setIcon(Node icon) {
        this.iconProperty().set(icon);
        icon.setId("icon");
        this.getContent().addChild(icon);
        HBox.setMargin(icon, new Insets(1, 3, 0, 0));
    }


    public Node getIcon() {
        return this.iconProperty == null ? null : this.iconProperty.get();
    }

    public Node getIconNode() {
        return this.getContent().lookup("icon");
    }

    private SimpleObjectProperty<String> nameProperty;

    public SimpleObjectProperty<String> nameProperty() {
        if (this.nameProperty == null) {
            this.nameProperty = new SimpleObjectProperty<>();
        }
        return this.nameProperty;
    }

    public void setName(String name) {
        this.nameProperty().set(name);
        FXText text = new FlexText(name);
        text.setId("name");
        this.getContent().addChild(text);
        HBox.setMargin(text, new Insets(1, 3, 0, 0));
    }

    public FlexHBox getContent() {
        FlexHBox content = (FlexHBox) this.lookup("#content");
        if (content == null) {
            content = new FlexHBox();
            content.setId("content");
            content.addChild(AvdTreeViewUtil.initRightIcon(this));
            VBox.setMargin(content, new Insets(0, 0, 0, this.level * 15));
            this.addChild(content);
        }
        return content;
    }

    public String getName() {
        return this.nameProperty == null ? null : this.nameProperty.get();
    }

    public FXText getNameNode() {
        return (FXText) this.getContent().lookup("name");
    }

    private SimpleBooleanProperty expanded;

    public void setExpanded(boolean value) {
        expandedProperty().setValue(value);
    }

    public boolean isExpanded() {
        return expanded != null && expanded.getValue();
    }

    public BooleanProperty expandedProperty() {
        if (expanded == null) {
            expanded = new SimpleBooleanProperty();
        }
        return expanded;
    }

    public void expand() {
        this.setExpanded(true);
        this.getContent().setChild(0, AvdTreeViewUtil.initDownIcon(this));
        for (Node child : this.getChildren()) {
            if (!Objects.equals(child.getId(), "content")) {
                child.setManaged(true);
                child.setVisible(true);
            }
        }
        AvdTreeViewUtil.updateHeight(this);
    }

    public void collapse() {
        this.setExpanded(false);
        this.getContent().setChild(0, AvdTreeViewUtil.initRightIcon(this));
        for (Node child : this.getChildren()) {
            if (!Objects.equals(child.getId(), "content")) {
                child.setManaged(false);
                child.setVisible(false);
            }
        }
        AvdTreeViewUtil.updateHeight(this);
    }

    /**
     * 子节点是否为空
     *
     * @return 结果
     */
    public boolean isChildEmpty() {
        return this.childItem == null || this.getChildItem().isEmpty();
    }
}
