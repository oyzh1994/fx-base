package cn.oyzh.fx.plus.operator;

import cn.oyzh.fx.plus.LimitLenControl;
import cn.oyzh.fx.plus.LimitLineControl;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * @author oyzh
 * @since 2024/6/21
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
