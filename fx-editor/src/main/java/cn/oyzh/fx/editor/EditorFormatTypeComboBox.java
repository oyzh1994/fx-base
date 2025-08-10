package cn.oyzh.fx.editor;


import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;

/**
 * redis格式下拉框
 *
 * @author oyzh
 * @since 2023/8/14
 */
public class EditorFormatTypeComboBox extends FXComboBox<EditorFormatType> implements I18nSelectAdapter<EditorFormatType> {

    {
        NodeManager.init(this);
        this.setTipText(I18nHelper.format());
        this.setConverter(new StringConverter<EditorFormatType>() {
            @Override
            public String toString(EditorFormatType formatType) {
                if (formatType == null) {
                    return "";
                }
                return formatType.getName();
            }

            @Override
            public EditorFormatType fromString(String s) {
                return null;
            }
        });
    }

    /**
     * 获取格式
     *
     * @return 格式
     */
    public EditorFormatType getFormat() {
        EditorFormatType type = this.getSelectedItem();
        return type == null ? EditorFormatType.RAW : type;
    }

    /**
     * 设置格式
     *
     * @param formatType 格式
     */
    public void setFormat(EditorFormatType formatType) {
        this.select(formatType);
    }

    @Override
    public List<EditorFormatType> values(Locale locale) {
        this.clearItems();
        for (EditorFormatType formatType : EditorFormatType.values()) {
            this.addItem(formatType);
        }
        return this.getItems();
    }
}
