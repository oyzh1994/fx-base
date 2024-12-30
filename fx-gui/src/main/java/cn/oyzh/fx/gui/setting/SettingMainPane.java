package cn.oyzh.fx.gui.setting;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import javafx.scene.Node;

/**
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingMainPane extends FlexHBox {

    public void setLeft(Node left) {
        SettingLeftContent content = (SettingLeftContent) this.getChild(0);
        if (content == null) {
            content = new SettingLeftContent(left);
            content.setFlexWidth("30%");
            content.setFlexHeight("100%");
            this.setChild(0, content);
        } else {
            content.setChild(0, left);
        }
    }

    public Node getLeft() {
        SettingLeftContent content = this.getLeftContent();
        return content.getChild(0);
    }

    public SettingLeftContent getLeftContent() {
        return (SettingLeftContent) this.getChild(0);
    }

    public SettingLeftTreeView getLeftTreeView() {
        SettingLeftContent  content = this.getLeftContent();
        if(content == null) {
            return null;
        }
        return (SettingLeftTreeView) content.lookup("#left-tree-view");
    }

    public void setRight(Node right) {
        SettingRightContent content = (SettingRightContent) this.getChild(1);
        if (content == null) {
            content = new SettingRightContent(right);
            content.setFlexWidth("70%");
            content.setFlexHeight("100%");
            this.setChild(1, content);
        } else {
            content.setChild(0, right);
        }
    }

    public Node getRight() {
        SettingRightContent content = this.getRightContent();
        return content.getChild(0);
    }

    public SettingRightContent getRightContent() {
        return (SettingRightContent) this.getChild(1);
    }

    public void setAction(SettingRightAction action) {
        SettingRightContent content = this.getRightContent();
        if (content != null) {
            content.setChild(1, action);
        } else {
            JulLog.warn("SettingRightContent is null");
        }
    }

    public SettingRightAction getAction() {
        SettingRightContent content = this.getRightContent();
        if (content == null) {
            return null;
        }
        return (SettingRightAction) content.getChild(1);
    }
}



