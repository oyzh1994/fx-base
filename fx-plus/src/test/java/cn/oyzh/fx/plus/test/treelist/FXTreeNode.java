package cn.oyzh.fx.plus.test.treelist;


import cn.oyzh.fx.plus.controls.box.FXHBox;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;


/**
 * @author oyzh
 * @since 2024-09-19
 */
public class FXTreeNode extends FXHBox {

    private FXHBox box;

    private byte level;

    private boolean leaf;

    private boolean visible;

    private boolean expanded;

    public FXTreeNode() {
        this.box = this;
        // this.box = new FXHBox();
    }

    public void setGraphic(Node graphic) {
        if (graphic != null) {
            graphic.getProperties().put("type", "graphic");
            this.box.addChild(graphic);
        }
    }

    public void setText(Node text) {
        if (text != null) {
            text.getProperties().put("type", "text");
            this.box.addChild(text);
        }
    }

    public void setExtra(Node extra) {
        if (extra != null) {
            extra.getProperties().put("type", "extra");
            this.box.addChild(extra);
        }
    }
}
