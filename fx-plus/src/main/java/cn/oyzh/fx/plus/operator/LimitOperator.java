package cn.oyzh.fx.plus.operator;

import cn.oyzh.fx.plus.LimitLenControl;
import cn.oyzh.fx.plus.LimitLineControl;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * 文本输入限制操作器，依据所在控件对输入变更进行行数与长度限制校验
 *
 * @author oyzh
 * @since 2024-06-21
 */
public class LimitOperator implements UnaryOperator<TextFormatter.Change> {

    @Override
    public TextFormatter.Change apply(TextFormatter.Change change) {
        if (this instanceof LimitLineControl control && !control.checkLineLimit(change)) {
            return null;
        }
        if (this instanceof LimitLenControl control && !control.checkLenLimit(change)) {
            return null;
        }
        return change;
    }
}
