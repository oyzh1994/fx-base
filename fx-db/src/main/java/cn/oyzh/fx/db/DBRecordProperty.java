package cn.oyzh.fx.db;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;

/**
 * 数据库表记录属性，绑定记录值的编辑控件并跟踪变更状态
 *
 * @author oyzh
 * @since 2024/01/31
 */
public class DBRecordProperty extends SimpleObjectProperty<Object> implements Destroyable {

    /**
     * 节点
     */
    protected Node node;

    /**
     * 原始数据
     */
    protected Object original;

    /**
     * 设置为null标志位
     */
    protected boolean setToNullFlag;

    /**
     * 只读模式
     */
    protected boolean readonly;

    /**
     * 是否变更
     */
    protected SimpleBooleanProperty changedProperty;

    /**
     * 构造数据库记录属性对象。
     */
    public DBRecordProperty() {
    }

    /**
     * 构造数据库记录属性对象。
     *
     * @param value 值
     */
    public DBRecordProperty(Object value) {
        super(value);
    }

    /**
     * 抛弃
     */
    public void discard() {
        this.setChanged(false);
    }

    /**
     * 获取变更属性。
     *
     * @return 变更属性
     */
    public SimpleBooleanProperty changedProperty() {
        if (this.changedProperty == null) {
            this.changedProperty = new SimpleBooleanProperty();
        }
        return this.changedProperty;
    }

    /**
     * 是否变更。
     *
     * @return 变更
     */
    public boolean isChanged() {
        return this.changedProperty != null && this.changedProperty.get();
    }

    /**
     * 设置变更。
     *
     * @param changed 是否变更
     */
    public void setChanged(boolean changed) {
        this.changedProperty().set(changed);
    }

    /**
     * 获取控件。
     *
     * @return 控件
     */
    public Node getControl() {
        return this.node;
    }

    /**
     * 复制。
     */
    public void vCopy() {
        ClipboardUtil.copy(this.node);
    }

    /**
     * 粘贴。
     */
    public void vPaste() {
        ClipboardUtil.paste(this.node);
    }

    /**
     * 获取原始。
     *
     * @return 原始
     */
    public Object getOriginal() {
        return original;
    }

    /**
     * 设置原始。
     *
     * @param original 原始
     */
    public void setOriginal(Object original) {
        this.original = original;
    }

    /**
     * 是否只读。
     *
     * @return 只读
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * 获取节点。
     *
     * @return 节点
     */
    public Node getNode() {
        return this.getControl();
    }

    /**
     * 设置为null字符串
     */
    public void vSetToNull() {
        if (this.node instanceof TextField textField) {
            // 如果内容为空，则直接设置变更
            if (StringUtil.isEmpty(textField.getText())) {
                this.setChanged(true);
            } else {
                textField.clear();
            }
            textField.setPromptText(DBUtil.nullPromptText());
            NodeUtil.unFocus(this.node);
        }
        this.setToNullFlag = true;
    }

    /**
     * 设置为空字符串
     */
    public void vSetToEmptyString() {
        if (this.node instanceof TextField textField) {
            // 如果内容为空，则直接设置变更
            if (StringUtil.isEmpty(textField.getText())) {
                this.setChanged(true);
            }
            textField.setText("");
            textField.setPromptText("");
            NodeUtil.unFocus(this.node);
        }
    }

    @Override
    public void destroy() {
        if (this.node != null) {
            NodeDestroyUtil.destroyObject(this.node);
            this.node = null;
            this.original = null;
            this.changedProperty.unbind();
            this.changedProperty = null;
        }
    }
}
