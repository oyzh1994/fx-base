package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import javafx.scene.Node;

/**
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingMainPane extends FlexHBox {

    public void setLeft(Node left) {
        FlexVBox vBox = (FlexVBox) this.getChild(0);
        if (vBox == null) {
            vBox = new FlexVBox(left);
            vBox.setFlexWidth("30%");
            vBox.setFlexHeight("100%");
            this.setChild(0, vBox);
        } else {
            vBox.setChild(0, left);
        }
    }

    public Node getLeft() {
        return this.getChild(0);
    }

    public void setRight(Node right) {
        FlexVBox vBox = (FlexVBox) this.getChild(1);
        if (vBox == null) {
            vBox = new FlexVBox(right);
            vBox.setFlexWidth("70%");
            vBox.setFlexHeight("100%");
            this.setChild(1, vBox);
        } else {
            vBox.setChild(0, right);
        }
    }

    public Node getRight() {
        FlexVBox vBox = (FlexVBox) this.getChild(1);
        if (vBox == null) {
            return null;
        }
        return vBox.getChild(0);
    }

    public void setAction(Node action) {
        FlexVBox vBox = (FlexVBox) this.getChild(1);
        if (vBox == null) {
            vBox = new FlexVBox(action);
            this.setChild(1, vBox);
        } else {
            vBox.setChild(1, action);
        }
    }

    public Node getAction() {
        FlexVBox vBox = (FlexVBox) this.getChild(1);
        if (vBox == null) {
            return null;
        }
        return vBox.getChild(1);
    }

}



