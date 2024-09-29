package cn.oyzh.fx.plus.test;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import javafx.scene.input.KeyEvent;

public class xx2 {

    public boolean check(KeyEvent event, String text) {
        StringBuilder textNew = new StringBuilder();
        if (StringUtil.isNotEmpty(event.getText())) {
            textNew.append(event.getText());
        } else if (StringUtil.isNotEmpty(event.getCharacter())) {
            textNew.append(event.getCharacter());
        }
        textNew.append(text);
        return NumberUtil.isNumber(textNew);
    }
}
