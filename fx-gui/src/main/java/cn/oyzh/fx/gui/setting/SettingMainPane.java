package cn.oyzh.fx.gui.setting;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingMainPane extends FXHBox {

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
        SettingLeftContent content = this.getLeftContent();
        if (content == null) {
            return null;
        }
        return (SettingLeftTreeView) content.lookup("#left-tree-view");
    }

    public void setRight(Node right) {
        SettingRightContent content = (SettingRightContent) this.getChild(1);
        if (content == null) {
            SettingRightNavBar navBar = new SettingRightNavBar();
            content = new SettingRightContent(navBar, right);
            VBox.setMargin(navBar, new Insets(0, 0, 10, 30));
            content.setFlexWidth("70%");
            content.setFlexHeight("100%");
            this.setChild(1, content);
        } else {
            content.setChild(1, right);
        }
    }

    public Node getRight() {
        SettingRightContent content = this.getRightContent();
        return content.getChild(1);
    }

    public SettingRightContent getRightContent() {
        return (SettingRightContent) this.getChild(1);
    }

    public void setAction(SettingRightAction action) {
        SettingRightContent content = this.getRightContent();
        if (content != null) {
            content.setChild(2, action);
        } else {
            JulLog.warn("SettingRightContent is null");
        }
    }

    public SettingRightAction getAction() {
        SettingRightContent content = this.getRightContent();
        if (content == null) {
            return null;
        }
        return (SettingRightAction) content.getChild(2);
    }

    public SettingRightNavBar getNavBar() {
        SettingRightContent content = this.getRightContent();
        if (content == null) {
            return null;
        }
        return (SettingRightNavBar) content.getChild(0);
    }

    void updateRightContent(String fxId, String label) {
        SettingRightContent rightContent = this.getRightContent();
        if (rightContent != null) {
            Node node = rightContent.lookup("#" + fxId);
            SettingRightNavBar navBar = this.getNavBar();
            if (node != null) {
                navBar.enable();
                NodeGroupUtil.enable(this, "setting_item");
                NodeGroupUtil.disappear(this, "setting_item");
                node.setVisible(true);
                navBar.setText(label);
            } else {
                navBar.disable();
                NodeGroupUtil.disable(this, "setting_item");
                JulLog.warn("node:{} is null", fxId);
            }
        } else {
            JulLog.warn("SettingRightContent is null");
        }
    }
}



