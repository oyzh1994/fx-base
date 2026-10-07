package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.NumberUtil;

/**
 * 二进制文本输入框，用于展示 BLOB 数据
 *
 * @author oyzh
 * @since 2024-07-10
 */
public class BinaryTextFiled extends ChooseFileTextField {

    /**
     * 文件大小换算基数
     */
    private Integer scale = 2;

    /**
     * 获取文件大小换算基数
     *
     * @return 换算基数
     */
    public Integer getScale() {
        return scale;
    }

    /**
     * 设置文件大小换算基数
     *
     * @param scale 换算基数
     */
    public void setScale(Integer scale) {
        this.scale = scale;
    }

    @Override
    public void formatValue() {
        this.setText(format(super.value(), this.scale));
    }

    /**
     * 将字节数组格式化为 BLOB 描述
     *
     * @param o     值
     * @param scale 换算基数
     * @return BLOB 描述
     */
    public static String format(Object o, Integer scale) {
        if (o == null) {
            return null;
        }
        if (o instanceof byte[] bytes) {
            return "(BLOB)" + " " + NumberUtil.formatSize(bytes.length, scale);
        }
        return "(BLOB)";
    }
}
