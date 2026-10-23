package cn.oyzh.fx.editor.incubator.control;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.skin.SelectTextFiledSkin;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑器格式类型输入框，支持输入过滤。
 * <p>
 * 替代原 EditorFormatTypeComboBox，格式较多时可输入名称、枚举名或扩展名进行过滤。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public class EditorFormatTypeTextField extends SelectTextFiled<EditorFormatType> {

    /**
     * 当前选中的格式
     */
    private final ObjectProperty<EditorFormatType> selectedFormat = new SimpleObjectProperty<>(EditorFormatType.RAW);

    /**
     * 选中变更监听器
     */
    private final List<ChangeListener<EditorFormatType>> changeListeners = new ArrayList<>();

    /**
     * 初始化前的待选中格式
     */
    private EditorFormatType pendingFormat;

    /**
     * 初始化下标
     */
    private int initIndex;

    /**
     * 是否已初始化
     */
    private boolean initialized;

    @Override
    protected SelectTextFiledSkin<EditorFormatType> createDefaultSkin() {
        return new EditorFormatTypeTextFieldSkin(this);
    }

    /**
     * 获取格式
     *
     * @return 格式
     */
    public EditorFormatType getFormat() {
        if (!this.initialized && this.pendingFormat != null) {
            return this.pendingFormat;
        }
        EditorFormatType format = this.selectedFormat.get();
        return format == null ? EditorFormatType.RAW : format;
    }

    /**
     * 设置格式
     *
     * @param formatType 格式
     */
    public void setFormat(EditorFormatType formatType) {
        EditorFormatType target = formatType == null ? EditorFormatType.RAW : formatType;
        if (this.initialized) {
            this.selectItem(target);
        } else {
            this.pendingFormat = target;
            this.selectedFormat.set(target);
        }
    }

    /**
     * 选中格式，兼容旧下拉框的select方法
     *
     * @param formatType 格式
     */
    public void select(EditorFormatType formatType) {
        this.setFormat(formatType);
    }

    /**
     * 选中格式，兼容旧下拉框的selectObj方法
     *
     * @param obj 格式对象或名称
     */
    public void selectObj(Object obj) {
        if (obj instanceof EditorFormatType formatType) {
            this.select(formatType);
            return;
        }
        if (obj != null) {
            String text = obj.toString();
            for (EditorFormatType formatType : EditorFormatType.values()) {
                if (StringUtil.equalsIgnoreCase(text, formatType.name())
                        || StringUtil.equalsIgnoreCase(text, formatType.getName())) {
                    this.select(formatType);
                    return;
                }
            }
        }
    }

    /**
     * 选中项变更监听，兼容旧下拉框的监听方法
     *
     * @param listener 监听器
     */
    public void selectedItemChanged(ChangeListener<EditorFormatType> listener) {
        if (listener != null) {
            this.changeListeners.add(listener);
        }
    }

    /**
     * 获取选中项属性
     *
     * @return 选中项属性
     */
    public ObjectProperty<EditorFormatType> selectedItemProperty() {
        return this.selectedFormat;
    }

    @Override
    public EditorFormatType getSelectedItem() {
        return this.getFormat();
    }

    /**
     * 设置初始化下标
     *
     * @param initIndex 下标
     */
    public void setInitIndex(int initIndex) {
        this.initIndex = initIndex;
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.format());
        // 转换器
        this.skin().setConverter(new StringConverter<>() {
            @Override
            public String toString(EditorFormatType formatType) {
                return formatType == null ? "" : formatType.getName();
            }

            @Override
            public EditorFormatType fromString(String string) {
                return null;
            }
        });
        // 初始候选项
        this.setItemList(new ArrayList<>(List.of(EditorFormatType.values())));
        super.initNode();
        // 选中变更
        super.selectedItemChanged(formatType -> {
            if (formatType == null) {
                return;
            }
            EditorFormatType oldValue = this.selectedFormat.get();
            this.selectedFormat.set(formatType);
            // 选中后恢复完整候选列表，避免下次打开只剩过滤结果
            this.setItemList(new ArrayList<>(List.of(EditorFormatType.values())));
            if (!formatType.equals(oldValue)) {
                for (ChangeListener<EditorFormatType> listener : this.changeListeners) {
                    listener.changed(this.selectedFormat, oldValue, formatType);
                }
            }
        });
        this.initialized = true;
        // 初始化选中项：优先format属性，其次initIndex
        EditorFormatType initial = this.pendingFormat;
        if (initial == null) {
            EditorFormatType[] values = EditorFormatType.values();
            int index = Math.max(0, Math.min(this.initIndex, values.length - 1));
            initial = values[index];
        }
        this.selectItem(initial);
    }

    @Override
    protected boolean onTextChanged(String newValue) {
        if (!super.onTextChanged(newValue)) {
            return false;
        }
        List<EditorFormatType> formats = EditorFormatType.filter(newValue);
        this.setItemList(formats);
        if (formats.isEmpty()) {
            this.skin().hidePopup();
        } else {
            this.skin().showPopup();
        }
        return true;
    }
}
