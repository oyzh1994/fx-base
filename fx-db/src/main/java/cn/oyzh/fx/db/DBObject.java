package cn.oyzh.fx.db;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.BooleanUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 数据库对象基类，维护对象的变更、新增、删除状态及原始数据
 *
 * @author oyzh
 * @since 2024-07-13
 */
public class DBObject implements Destroyable {

    /**
     * 对象是否变更
     */
    private SimpleBooleanProperty changedProperty;

    /**
     * 对象是否删除
     */
    private SimpleBooleanProperty deletedProperty;

    /**
     * 对象是否新增
     */
    private SimpleBooleanProperty createdProperty;

    /**
     * 变更数据
     */
    private Map<String, Boolean> changedFlag;

    /**
     * 原始数据
     */
    private Map<String, Object> originalData;

    /**
     * 获取变更属性
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
     * 获取删除属性
     *
     * @return 删除属性
     */
    public SimpleBooleanProperty deletedProperty() {
        if (this.deletedProperty == null) {
            this.deletedProperty = new SimpleBooleanProperty();
        }
        return this.deletedProperty;
    }

    /**
     * 获取新增属性
     *
     * @return 新增属性
     */
    public SimpleBooleanProperty createdProperty() {
        if (this.createdProperty == null) {
            this.createdProperty = new SimpleBooleanProperty();
        }
        return this.createdProperty;
    }

    /**
     * 获取变更标记集合
     *
     * @return 变更标记集合
     */
    private Map<String, Boolean> changedFlag() {
        if (this.changedFlag == null) {
            this.changedFlag = new HashMap<>();
        }
        return this.changedFlag;
    }

    /**
     * 设置指定键的变更标记
     *
     * @param key   键
     * @param value 变更标记
     */
    private void setChangedFlag(String key, Boolean value) {
        // 已变更
        if (BooleanUtil.isTrue(value)) {
            this.changedFlag().put(key, value);
        } else {// 未变更
            this.changedFlag().remove(key);
        }
        this.setChanged(!this.changedFlag().isEmpty());
    }

    /**
     * 清空变更标记
     */
    protected void clearChangedFlag() {
        if (this.changedFlag != null) {
            this.changedFlag().clear();
        }
    }

    /**
     * 获取原始数据集合
     *
     * @return 原始数据集合
     */
    protected Map<String, Object> originalData() {
        if (this.originalData == null) {
            this.originalData = new HashMap<>();
        }
        return this.originalData;
    }

    /**
     * 记录原始数据，首次记录不比较，后续记录时比较差异并更新变更标记
     *
     * @param key   键
     * @param value 值
     */
    protected void putOriginalData(String key, Object value) {
        // JulLog.info("putOriginalData: key={}, value={}", key, value);
        if (this.originalData().containsKey(key)) {
            Object val = this.getOriginalData(key);
            this.setChangedFlag(key, !Objects.equals(val, value));
        } else {
            this.originalData().put(key, value);
        }
    }

    /**
     * 获取指定键的原始数据
     *
     * @param key 键
     * @return 原始数据
     */
    protected Object getOriginalData(String key) {
        if (this.originalData == null) {
            return null;
        }
        return this.originalData().get(key);
    }

    /**
     * 是否有原始数据
     *
     * @param key 键
     * @return 结果
     */
    protected boolean hasOriginalData(String key) {
        if (this.originalData == null) {
            return false;
        }
        return this.originalData().containsKey(key);
    }

    /**
     * 清空原始数据
     */
    public void clearOriginalData() {
        if (this.originalData != null) {
            this.originalData().clear();
        }
    }

    /**
     * 比较指定键的原始数据与当前数据是否发生变更
     *
     * @param key         键
     * @param currentData 当前数据
     * @return 结果
     */
    protected boolean checkOriginalData(String key, Object currentData) {
        if (!this.hasOriginalData(key)) {
            return false;
        }
        return !Objects.equals(this.getOriginalData(key), currentData);
    }

    /**
     * 初始化状态
     */
    public void initStatus() {

    }

    /**
     * 设置变更。
     *
     * @param changed 是否变更
     */
    public void setChanged(boolean changed) {
        this.changedProperty().set(changed);
        this.updateStatus();
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
     * 设置删除。
     *
     * @param deleted 删除
     */
    public void setDeleted(boolean deleted) {
        this.deletedProperty().set(deleted);
        this.updateStatus();
    }

    /**
     * 是否删除。
     *
     * @return 删除
     */
    public boolean isDeleted() {
        return this.deletedProperty != null && this.deletedProperty.get();
    }

    /**
     * 设置创建。
     *
     * @param created 创建
     */
    public void setCreated(boolean created) {
        this.createdProperty().set(created);
        this.updateStatus();
    }

    /**
     * 是否创建。
     *
     * @return 创建
     */
    public boolean isCreated() {
        return this.createdProperty != null && this.createdProperty.get();
    }

    /**
     * 清空状态
     */
    public void clearStatus() {
        this.setChanged(false);
        this.setCreated(false);
        this.setDeleted(false);
        this.updateStatus();
        this.clearChangedFlag();
    }

    /**
     * 根据新增、变更状态刷新状态标识
     */
    public void updateStatus() {
        if (this.isCreated()) {
            this.statusProperty().set("+");
        } else if (this.isChanged()) {
            this.statusProperty().set("*");
        } else {
            this.statusProperty().set("");
        }
    }

    /**
     * 状态属性
     */
    private SimpleStringProperty statusProperty;

    /**
     * 获取状态属性
     *
     * @return 状态属性
     */
    public SimpleStringProperty statusProperty() {
        if (this.statusProperty == null) {
            this.statusProperty = new SimpleStringProperty();
        }
        return statusProperty;
    }

    /**
     * 获取状态标识
     *
     * @return 状态标识
     */
    public String getStatus() {
        return this.statusProperty().get();
    }

    @Override
    public void destroy() {
        if (this.changedProperty != null) {
            this.changedProperty.unbind();
            this.changedProperty = null;
        }
        if (this.deletedProperty != null) {
            this.deletedProperty.unbind();
            this.deletedProperty = null;
        }
        if (this.createdProperty != null) {
            this.createdProperty.unbind();
            this.createdProperty = null;
        }
        if (this.statusProperty != null) {
            this.statusProperty.unbind();
            this.statusProperty = null;
        }
        if (this.changedFlag != null) {
            this.changedFlag.clear();
            this.changedFlag = null;
        }
        if (this.originalData != null) {
            this.originalData.clear();
            this.originalData = null;
        }
    }
}
