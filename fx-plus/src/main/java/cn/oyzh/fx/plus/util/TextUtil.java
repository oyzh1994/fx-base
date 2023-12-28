package cn.oyzh.fx.plus.util;


import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * 文本工具类
 *
 * @author oyzh
 * @since 2022/12/27
 */
@UtilityClass
public class TextUtil {

    public static boolean checkNumber(KeyEvent event, TextField field) {
        String text0 = null;
        if (StrUtil.isNotEmpty(event.getText())) {
            text0 = event.getText();
        } else if (StrUtil.isNotEmpty(event.getCharacter())) {
            text0 = event.getCharacter();
        }
        String text1 = field.getText();
        IndexRange range = field.getSelection();
        String text2 = text1.substring(0, range.getStart());
        String text3 = text1.substring(range.getEnd());

        String nText = text2 + text0 + text3;
        System.out.println("nText==========" + nText);
        if (StrUtil.equalsAny(nText, "+", "-", "")) {
            return true;
        }
        return NumberUtil.isNumber(nText);
    }

    public static boolean checkDecimal(KeyEvent event, String text, Double max, Double min) {
        StringBuilder textNew = new StringBuilder();
        if (StrUtil.isNotEmpty(event.getText())) {
            textNew.append(event.getText());
        } else if (StrUtil.isNotEmpty(event.getCharacter())) {
            textNew.append(event.getCharacter());
        }
        textNew.append(text);
        String nText = textNew.toString();
        if (StrUtil.equalsAny(nText, ".", "+", "-")) {
            return true;
        }
        return NumberUtil.isNumber(textNew);
    }

    public static BigDecimal wrapValue(BigDecimal var0, BigDecimal var1, BigDecimal var2) {
        if (var2.doubleValue() == 0.0) {
            throw new RuntimeException();
        } else if (var0.compareTo(var1) < 0) {
            return var2;
        } else {
            return var0.compareTo(var2) > 0 ? var1 : var0;
        }
    }
}
