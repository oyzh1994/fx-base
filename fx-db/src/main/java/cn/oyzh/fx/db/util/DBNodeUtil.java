package cn.oyzh.fx.db.util;

import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.editor.incubator.control.JsonTextFiled;
import cn.oyzh.fx.editor.incubator.control.LongTextFiled;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.gui.text.field.BitTextField;
import cn.oyzh.fx.gui.text.field.BooleanTextFiled;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.DateTextField;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.ExampleTextField;
import cn.oyzh.fx.gui.text.field.LimitTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.TimeTextField;
import cn.oyzh.fx.gui.text.field.YearTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.CodeArea;

/**
 *
 * @author oyzh
 * @since 2026-09-06
 */
public class DBNodeUtil {

    /**
     * 获取节点值
     *
     * @param node 节点
     * @return 结果
     */
    public static Object getNodeVal(Node node) {
        Object val = null;
        if (node instanceof FXTextField textField) {
            val = textField.getValue();
            if (val == null) {
                val = textField.getText();
            }
        } else if (node instanceof TextField textField) {
            val = textField.getText();
        } else if (node instanceof TextArea textField) {
            val = textField.getText();
        } else if (node instanceof CodeArea textField) {
            val = textField.getText();
        } else if (node instanceof ComboBox<?> comboBox) {
            val = comboBox.getSelectionModel().getSelectedItem();
        }
        return val;
    }

    /**
     * 设置节点值
     *
     * @param node 节点
     * @param val  值
     */
    public static void setNodeVal(Node node, Object val) {
        if (node == null || val == null) {
            return;
        }
        if (node instanceof FXTextField textField) {
            textField.setValue(val);
        } else if (node instanceof TextField textField) {
            textField.setText(val.toString());
        } else if (node instanceof TextArea textField) {
            textField.setText(val.toString());
        } else if (node instanceof CodeArea textField) {
            textField.setText(val.toString());
        } else if (node instanceof ComboBox textField) {
            textField.getSelectionModel().select(val);
        }
    }

    /**
     * 获取背景颜色
     *
     * @param column 字段
     * @return 结果
     */
    public static Color getNodeBackground(DBColumn column) {
        if (column.supportJson()) {
            return Color.valueOf("#C9E4E8");
        }
        if (column.supportJsonArray()) {
            return Color.valueOf("#FDE5CF");
        }
        if (column.supportText()) {
            return Color.valueOf("#A1C9D1");
        }
        if (column.supportBinary()) {
            return Color.valueOf("#FBF0D0");
        }
        if (column.supportEnum()) {
            return Color.valueOf("#E8E0F0");
        }
        if (column.supportInteger()) {
            return Color.valueOf("#D7EED0");
        }
        if (column.supportBigInteger()) {
            return Color.valueOf("#CBE8C3");
        }
        if (column.supportDigits()) {
            return Color.valueOf("#CDECFA");
        }
        if (column.supportBit()) {
            return Color.valueOf("#FCE1E4");
        }
        if (column.supportBoolean()) {
            return Color.valueOf("#FAE1E4");
        }
        if (column.supportTimestamp()
                || column.isDateType()
                || column.isTimeType()
                || column.isYearType()
                || column.isDateTimeType()
        ) {
            return Color.valueOf("#F1E1F5");
        }
        if (column.supportGeometry()) {
            return Color.valueOf("#D4E8D0");
        }
        return Color.valueOf("#FDD4D3");
    }

    /**
     * 获取节点
     *
     * @param object 对象
     * @param column 字段
     * @return 节点
     */
    public static Node getNode(Object object, DBColumn column) {
        Node node;
        if (column.supportJson()) {
            JsonTextFiled textField = new JsonTextFiled();
            textField.setValue(object);
            node = textField;
        } else if (column.supportJsonArray()) {
            JsonTextFiled textField = new JsonTextFiled();
            textField.setValue(object);
            textField.setArray(true);
            node = textField;
        } else if (column.supportText()) {
            LongTextFiled textField = new LongTextFiled();
            textField.setValue(object);
            node = textField;
        } else if (column.supportText()) {
            LongTextFiled textField = new LongTextFiled();
            textField.setValue(object);
            node = textField;
        } else if (column.supportBinary()) {
            BinaryTextFiled textField = new BinaryTextFiled();
            textField.setValue(object);
            node = textField;
        } else if (column.supportInteger() || column.supportBigInteger()) {
            NumberTextField textField = new NumberTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportDigits()) {
            DecimalTextField textField = new DecimalTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportBit()) {
            BitTextField textField = new BitTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportBoolean()) {
            BooleanTextFiled textField = new BooleanTextFiled();
            textField.setValue(object);
            node = textField;
        } else if (column.supportTimestamp()) {
            DateTimeTextField textField = new DateTimeTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportGeometry()) {
            ExampleTextField textField = new ExampleTextField();
            textField.setExample(column.exampleValue());
            textField.setValue(object);
            node = textField;
        } else if (column.supportString()) {
            LimitTextField textField;
            if (column.supportSize() && column.getSize() != null) {
                textField = new LimitTextField((long) column.getSize());
            } else {
                textField = new LimitTextField();
            }
            textField.setValue(object);
            node = textField;
        } else if (column.isDateType()) {
            DateTextField textField = new DateTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.isTimeType()) {
            TimeTextField textField = new TimeTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.isYearType()) {
            YearTextField textField = new YearTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.isDateTimeType()) {
            DateTimeTextField textField = new DateTimeTextField();
            textField.setValue(object);
            node = textField;
        } else {
            FXTextField textField = new FXTextField();
            textField.setValue(object);
            node = textField;
        }
        if (node instanceof FXTextField textField) {
            if (textField.getBackground() == null) {
                Background bg = ControlUtil.background(DBNodeUtil.getNodeBackground(column));
                textField.setBackground(bg);
            }
        }
        return node;
    }

    /**
     * 生成节点
     *
     * @param column 字段
     * @return 结果
     */
    public static Node generateNode(DBColumn column) {
        Node node;
        if (column == null) {
            node = new FXTextField();
        } else if (column.supportJson()) {
            node = new JsonTextFiled();
        } else if (column.supportJsonArray()) {
            JsonTextFiled textFiled = new JsonTextFiled();
            textFiled.setArray(true);
            node = textFiled;
        } else if (column.supportString()) {
            if (column.supportSize() && column.getSize() != null) {
                node = new ClearableTextField((long) column.getSize());
            } else {
                node = new ClearableTextField();
            }
        } else if (column.supportBit()) {
            if (column.getSize() != null) {
                node = new BitTextField((long) column.getSize() * 8L);
            } else {
                node = new BitTextField();
            }
        } else if (column.supportBoolean()) {
            node = new BooleanTextFiled();
        } else if (column.supportInteger() || column.supportBigInteger()) {
            Integer size = column.getSize();
            node = new NumberTextField(size == null ? null : size.longValue(), column.minValue(), column.maxValue());
        } else if (column.supportDigits()) {
            Integer size = column.getSize();
            node = new DecimalTextField(size == null ? null : size.longValue(), column.minValue(), column.maxValue(), column.getDigits());
        } else if (column.isYearType()) {
            node = new YearTextField();
        } else if (column.isTimeType()) {
            node = new TimeTextField();
        } else if (column.isDateType()) {
            node = new DateTextField();
        } else if (column.supportTimestamp()) {
            node = new DateTimeTextField();
        } else if (column.supportBinary()) {
            node = new ChooseFileTextField();
        } else {
            node = new FXTextField();
        }
        return node;
    }
}
