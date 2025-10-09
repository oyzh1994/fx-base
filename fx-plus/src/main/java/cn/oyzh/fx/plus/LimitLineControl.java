package cn.oyzh.fx.plus;

import cn.oyzh.common.util.NumberUtil;
import javafx.scene.control.TextFormatter;

/**
 * 范围控制
 *
 * @author oyzh
 * @since 2024/06/21
 */
public interface LimitLineControl {

    /**
     * 检查边界
     *
     * @param change 更新内容
     * @return 结果
     */
    default boolean checkLineLimit(TextFormatter.Change change) {
        String text = change.getControlNewText();
        if (text.isEmpty()) {
            return true;
        }
        if (this.getMaxLine() != null) {
            try {
                var count = text.lines().count();
                // 新增
                if (change.isAdded() && NumberUtil.isGTEq(count, this.getMaxLine())) {
                    return false;
                }
                // 替换
                if (change.isReplaced() && NumberUtil.isGTEq(text.length(), this.getMaxLine())) {
                    return false;
                }
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    Long getMaxLine();

    void setMaxLine(Long maxLen);
}
