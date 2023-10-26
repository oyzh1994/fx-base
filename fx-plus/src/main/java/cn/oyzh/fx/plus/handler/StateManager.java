package cn.oyzh.fx.plus.handler;

import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;

/**
 * 状态管理器
 * @author oyzh
 * @since 2023/10/26
 */
public class StateManager {

    /**
     * managed属性绑定visible属性
     */
    @Getter
    private boolean managedBindVisible;

    /**
     * 设置managed属性绑定visible属性
     * @param managedBindVisible managed属性绑定visible属性
     */
    public void setManagedBindVisible(boolean managedBindVisible) {
        this.managedBindVisible = managedBindVisible;
        if (managedBindVisible) {
            if (!this.isVisible() && this.isManaged()) {
                this.setManaged(false);
            } else if (this.isManaged() && !this.isManaged()) {
                this.setManaged(true);
            }
            this.managedProperty().bind(this.visibleProperty());
        } else {
            this.managedProperty().unbind();
        }
    }

    /**
     * visible属性
     */
    private SimpleBooleanProperty visibleProperty;

    public SimpleBooleanProperty visibleProperty() {
        if (this.visibleProperty == null) {
            this.visibleProperty = new SimpleBooleanProperty(true);
        }
        return this.visibleProperty;
    }

    public boolean isVisible() {
        return this.visibleProperty == null || this.visibleProperty.get();
    }

    public void setVisible(boolean visible) {
        this.visibleProperty().set(visible);
    }

    /**
     * managed属性
     */
    private SimpleBooleanProperty managedProperty;

    public SimpleBooleanProperty managedProperty() {
        if (this.managedProperty == null) {
            this.managedProperty = new SimpleBooleanProperty(true);
        }
        return this.managedProperty;
    }

    public boolean isManaged() {
        return this.managedProperty == null || this.managedProperty.get();
    }

    public void setManaged(boolean managed) {
        this.managedProperty().set(managed);
    }

    /**
     * disable属性
     */
    private SimpleBooleanProperty disableProperty;

    public SimpleBooleanProperty disableProperty() {
        if (this.disableProperty == null) {
            this.disableProperty = new SimpleBooleanProperty(false);
        }
        return this.disableProperty;
    }

    public boolean isDisable() {
        return this.managedProperty != null && this.managedProperty.get();
    }

    public void setDisable(boolean disable) {
        this.disableProperty().set(disable);
    }

    public void disable() {
        this.setDisable(true);
    }

    public void enable() {
        this.setDisable(false);
    }
}
