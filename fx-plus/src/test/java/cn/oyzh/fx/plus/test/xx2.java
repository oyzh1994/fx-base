package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.common.util.RegexUtil;
import cn.oyzh.fx.common.util.StringUtil;
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
        return RegexUtil.isNumber(textNew.toString());
    }
}
