package cn.oyzh.fx.plus.tree;

import cn.oyzh.fx.plus.controls.FlexVBox;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.controls.text.FlexText;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import lombok.Getter;

/**
 * @author oyzh
 * @since 2023/12/5
 */
public class AvdTreeItem<E> extends FlexVBox {

    private final byte level;

    private AvdTreeItemList<AvdTreeItem<?>> child;

    @Getter
    private final AvdTreeItem<?> parent;

    public AvdTreeItem(AvdTreeItem<?> parent) {
        this.parent = parent;
        if (parent != null) {
            this.level = 0;
        } else {
            this.level = (byte) (parent.level + 1);
        }
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
        this.addChild(icon);
    }

    public Node getIcon() {
        return this.iconProperty == null ? null : this.iconProperty.get();
    }

    public Node getIconNode() {
        return this.lookup("#icon");
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
        this.addChild(text);
    }

    public String getName() {
        return this.nameProperty == null ? null : this.nameProperty.get();
    }

    public FXText getNameNode() {
        return (FXText) this.lookup("#name");
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
    }

    public void collapse() {
        this.setExpanded(false);
    }

    /**
     * 子节点是否为空
     *
     * @return 结果
     */
    public boolean isChildEmpty() {
        return this.child == null || this.getChild().isEmpty();
    }

    public AvdTreeItemList<AvdTreeItem<?>> getChild() {
        if (this.child == null) {
            this.child = new AvdTreeItemList<>();
        }
        return this.child;
    }

}
