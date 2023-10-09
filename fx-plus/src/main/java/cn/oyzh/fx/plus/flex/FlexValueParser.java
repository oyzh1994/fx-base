package cn.oyzh.fx.plus.flex;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.Parser;

/**
 * 流式布局值解析器
 *
 * @author oyzh
 * @since 2022/12/31
 */
public class FlexValueParser implements Parser<String, FlexValue> {

    /**
     * 当前实例
     */
    public static final FlexValueParser INSTANCE = new FlexValueParser();

    @Override
    public FlexValue parse(String flexValue) {
        if (StrUtil.isBlank(flexValue)) {
            return null;
        }
        flexValue = flexValue.trim();
        FlexValue config = new FlexValue();
        Double fixed = null;
        Double percent = null;
        Byte fixedOperator = null;
        try {
            // 包含百分比
            if (flexValue.contains("%")) {
                String[] strArr = flexValue.split("%");
                // 百分比值
                percent = Double.parseDouble(strArr[0].trim()) * 0.01;
                // 固定值
                if (strArr.length > 1) {
                    String str2 = strArr[1].trim();
                    if (str2.contains("+")) {
                        fixed = Double.parseDouble(str2.replace("+", "").trim());
                        fixedOperator = 0;
                    } else if (str2.contains("-")) {
                        fixed = Double.parseDouble(str2.replace("-", "").trim());
                        fixedOperator = 1;
                    } else if (str2.contains("*")) {
                        fixed = Double.parseDouble(str2.replace("*", "").trim());
                        fixedOperator = 2;
                    } else if (str2.contains("/")) {
                        fixed = Double.parseDouble(str2.replace("/", "").trim());
                        fixedOperator = 3;
                    }
                }
            } else {// 直接计算固定值
                fixed = Double.parseDouble(flexValue);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        config.fixed(fixed);
        config.percent(percent);
        config.fixedOperator(fixedOperator);
        return config;
    }
}
