package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.NumberUtil;

/**
 * @author oyzh
 * @since 2024/7/10
 */
public class BinaryTextFiled extends ChooseFileTextField {

    private Integer scale = 2;

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    @Override
    public void setValue(Object val) {
        super.setValue(val);
        this.setText(format(val, this.scale));
    }

    public static String format(Object o, Integer scale) {
        if (o instanceof byte[] bytes) {
            return "(BLOB)" + " " + NumberUtil.formatSize(bytes.length, scale);
        }
        return "(BLOB)";
    }
}
