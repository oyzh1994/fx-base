package cn.oyzh.fx.plus.test;

import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import javafx.scene.input.KeyEvent;

/**
 * 键盘输入数字校验的测试类
 *
 * @author oyzh
 * @since 2023-12-27
 */
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
